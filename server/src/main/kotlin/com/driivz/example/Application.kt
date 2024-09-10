package com.driivz.example

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.ChargerFindRequest
import com.driivz.example.api.ChargersResponse
import com.driivz.example.api.LoginRequest
import com.driivz.example.api.LoginResponse
import com.driivz.example.api.OneTimePaymentTransactionResponse
import com.driivz.example.api.PaymentCardsResponse
import com.driivz.example.api.SiteSearchRequest
import com.driivz.example.api.SitesResponse
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.log.logRequestAndResponse
import com.driivz.example.manager.ServiceAccountManager
import com.driivz.example.manager.StripeService
import com.driivz.example.security.JwtConfig
import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.model.EphemeralKey
import com.stripe.model.SetupIntent
import com.stripe.net.RequestOptions.RequestOptionsBuilder
import com.stripe.param.CustomerCreateParams
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json


fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val config = HoconApplicationConfig(ConfigFactory.load())
    val serviceAccountManager = ServiceAccountManager(config)
    val stripeService = StripeService(config)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(DoubleReceive)
    intercept(ApplicationCallPipeline.Monitoring) {
        logRequestAndResponse()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor sample app"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("ticket").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }


    routing {
        authenticate("auth-jwt") {
            get("/payment-methods") {
                val principal = call.principal<JWTPrincipal>()
                val accountNumber = principal?.payload?.getClaim("accountNumber")?.asInt()

                val serviceAccount = serviceAccountManager.getServiceAccount()

                if (serviceAccount != null) {
                    val paymentMethods = accountNumber?.let { serviceAccount.paymentMethods(it) }
                    if (paymentMethods != null) {
                        call.respond(PaymentCardsResponse(paymentMethods))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Fetch payment methods failed")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
                }
            }

            post("/add-payment") {
                val addPaymentCardRequest = call.receive<AddPaymentCardRequest>()
                stripeService.authorizeStripePayment(addPaymentCardRequest)

                val principal = call.principal<JWTPrincipal>()
                val accountNumber = principal?.payload?.getClaim("accountNumber")?.asInt()

                val serviceAccount = serviceAccountManager.getServiceAccount()

                if (serviceAccount != null && accountNumber != null) {
                    val addedPaymentMethod = serviceAccount.addPayment(accountNumber.toString(), addPaymentCardRequest)
                    if (addedPaymentMethod != null) {
                        call.respond(PaymentCardsResponse(listOf(addedPaymentMethod)))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Add payment method failed")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
                }
            }
        }

        get("/stripe/client-secret") {
            val configs = arrayOf(
                config.property("ktor.stripe.privateKey").getString(),
                config.property("ktor.stripe.publicKey").getString()
            )
            val serverApiPrivateKey = configs.first()
            val paypageId = configs.last()

            try {
                Stripe.apiKey = serverApiPrivateKey

                val params = CustomerCreateParams.builder().build()
                val customer = Customer.create(params)

                val customerMap = mapOf("customer" to customer.id)
                val intent = SetupIntent.create(customerMap)

                val requestOptions =
                    RequestOptionsBuilder()
                        .setStripeVersionOverride(Stripe.API_VERSION)
                        .build()
                val key = EphemeralKey.create(customerMap, requestOptions)

                call.respond(StripeSecretResponse(paypageId, intent.clientSecret, customer.id, key.rawJson))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.localizedMessage}")
            }
        }

        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val serviceAccount = serviceAccountManager.getServiceAccount()

            // Use the service account to perform login or other operations
            if (serviceAccount != null) {
                val accountNumber =
                    serviceAccount.findCustomerAccountNumber(loginRequest.username)

                //TODO: verify the driver's password somehow - store/sync driver entities in your database

                // login with the driver account number - get the driver token
                val ticket = accountNumber?.let { serviceAccount.loginAsCustomer(it) }

                if (ticket != null) {
                    val token = JwtConfig.generateToken(ticket, accountNumber)
                    call.respond(LoginResponse(token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Driver account login failed")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
            }
        }

        get("/sites") {
            val searchRequest = call.receive<SiteSearchRequest>()
            val serviceAccount = serviceAccountManager.getServiceAccount()

            if (serviceAccount != null) {
                val siteList = serviceAccount.searchSites(searchRequest)
                if (siteList != null) {
                    call.respond(SitesResponse(siteList))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Fetch of sites failed")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
            }
        }

        get("/site/{siteId}/chargers") {
            val siteId = call.parameters["siteId"]?.toLong()
            val serviceAccount = serviceAccountManager.getServiceAccount()

            if (serviceAccount != null) {
                val site = serviceAccount.findSite(siteId)
                val findRequest = ChargerFindRequest(site?.chargerIds)

                val chargerList = serviceAccount.findChargerLocations(findRequest)
                if (chargerList != null) {
                    call.respond(ChargersResponse(chargerList))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Fetch of chargers failed")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
            }
        }

        post("/charger/otp/{chargerId}") {
            val chargerId = call.parameters["chargerId"]?.toLong()
            val serviceAccount = serviceAccountManager.getServiceAccount()

            if (serviceAccount != null) {
                val chargerProfile = serviceAccount.findChargerProfile(chargerId)
                val connector = chargerProfile?.evses?.firstOrNull()?.connectors?.firstOrNull()

                if (connector != null) {
                    val addPaymentCardRequest = call.receive<AddPaymentCardRequest>()
                    stripeService.authorizeStripePayment(addPaymentCardRequest)

                    val transaction = serviceAccount.oneTimePaymentStartTransaction(connector.id, addPaymentCardRequest)
                    if (transaction != null) {
                        call.respond(OneTimePaymentTransactionResponse(transaction))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Transaction failed")
                    }
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Fetch of charger profile failed")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
            }
        }
    }


}



