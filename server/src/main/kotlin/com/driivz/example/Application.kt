package com.driivz.example

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.LoginRequest
import com.driivz.example.api.LoginResponse
import com.driivz.example.api.PaymentCardsResponse
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.log.logRequestAndResponse
import com.driivz.example.manager.ServiceAccountManager
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

                val principal = call.principal<JWTPrincipal>()
                val accountNumber = principal?.payload?.getClaim("accountNumber")?.asInt()

                val serviceAccount = serviceAccountManager.getServiceAccount()

                if (serviceAccount != null) {
                    accountNumber.let { addPaymentCardRequest.accountNumber = it }

                    val paymentMethod = serviceAccount.addPayment(addPaymentCardRequest)
                    if (paymentMethod != null) {
                        call.respond(PaymentCardsResponse(listOf(paymentMethod)))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Add payment method failed")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Service account login failed")
                }
            }
        }

        get("/stripe/client-secret") {
            val serviceAccount = serviceAccountManager.getServiceAccount()

            //TODO: server is not returning configurations - bug with api-gateway
            //val configs = serviceAccount?.configurationValues(listOf("clearingHouseServerApiPrivateKey",
            //    "clearingHousePaypageId"))
            val configs = arrayOf(
                // clearingHouseServerApiPrivateKey
                "***REMOVED***",
                // clearingHousePaypageId
                "***REMOVED***"
            )
            val serverApiPrivateKey = configs?.first()
            val paypageId = configs?.last()

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

                //TODO: verify the driver's password somehow - can store credentials if needed

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
    }
}

