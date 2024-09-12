import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.Charger
import com.driivz.example.api.ChargerFindRequest
import com.driivz.example.api.ChargerFindResponse
import com.driivz.example.api.ChargerProfile
import com.driivz.example.api.ChargerProfileFindResponse
import com.driivz.example.api.CustomerAccountFilterRequest
import com.driivz.example.api.CustomerAccountFilterResponse
import com.driivz.example.api.CustomerLoginRequest
import com.driivz.example.api.OneTimePaymentStartTransaction
import com.driivz.example.api.OneTimePaymentStartTransactionResponse
import com.driivz.example.api.PaymentCard
import com.driivz.example.api.PaymentCardResponse
import com.driivz.example.api.Site
import com.driivz.example.api.SiteSearchRequest
import com.driivz.example.api.SiteSearchResponse
import com.driivz.example.security.ServiceAccount
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ServiceAccountImpl(
    private val userName: String,
    private val password: String,
    private val baseURL: String
) : ServiceAccount {

    override var ticket: String = ""
    override var expirationTime: Long = 0

    override suspend fun login(): Boolean {
        val client = httpClient()
        val response: HttpResponse = client.post("${baseURL}v1/authentication/operator/login") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "userName" to userName,
                    "password" to password
                )
            )
        }

        return if (response.status == HttpStatusCode.OK) {
            val responseBody = response.body<ServiceAccountResponse>()
            if (responseBody.httpStatusCode == 200 && responseBody.data.isNotEmpty()) {
                ticket = responseBody.data.first().ticket
                expirationTime = System.currentTimeMillis() + 10 * 60 * 60 * 1000 // 10 hours in milliseconds
                client.close()
                true
            } else {
                client.close()
                false
            }
        } else {
            client.close()
            false
        }
    }

    override suspend fun findCustomerAccountNumber(userName: String): Int? {
        val client = httpClient()

        val request = CustomerAccountFilterRequest(
            email = userName,
            isOtp = false
        )

        return try {
            val response = client.post("${baseURL}v1/customer-accounts/filter") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<CustomerAccountFilterResponse>()

            // Assuming you want the first account number from the data list
            response.data?.firstOrNull()?.accountNumber

        } catch (e: Exception) {
            println("Error fetching customer accounts: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun loginAsCustomer(accountNumber: Int): String? {
        val client = httpClient()

        val request = CustomerLoginRequest(
            accountNumber = accountNumber
        )

        return try {
            val response = client.post("${baseURL}v1/authentication/operator/customer-login") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<ServiceAccountResponse>()

            response.data.firstOrNull()?.ticket

        } catch (e: Exception) {
            println("Error fetching customer accounts: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun paymentMethods(accountNumber: Int): List<PaymentCard>? {
        val client = httpClient()

        return try {
            val response = client.get("${baseURL}v1/accounts/${accountNumber}/payment-methods/payment-cards") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
            }.body<PaymentCardResponse>()

            response.data

        } catch (e: Exception) {
            println("Error fetching customer payments: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun addPayment(accountNumber: String, request: AddPaymentCardRequest): PaymentCard? {
        val client = httpClient()

        return try {
            val response = client.post("${baseURL}v1/accounts/${accountNumber}/payment-methods/payment-cards") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<PaymentCardResponse>()

            response.data?.firstOrNull()

        } catch (e: Exception) {
            println("Error adding payment: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun findSite(siteId: Long?): Site? {
        val client = httpClient()

        return try {
            val response = client.get("${baseURL}v1/sites/${siteId}") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
            }.body<SiteSearchResponse>()

            response.data?.firstOrNull()

        } catch (e: Exception) {
            println("Error finding site ID ${siteId}: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun findChargerProfile(chargerId: Long?): ChargerProfile? {
        val client = httpClient()

        return try {
            val response = client.get("${baseURL}v1/chargers/${chargerId}/profile") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
            }.body<ChargerProfileFindResponse>()

            response.data?.firstOrNull()

        } catch (e: Exception) {
            println("Error finding charger profile ID ${chargerId}: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun searchSites(request: SiteSearchRequest): List<Site>? {
        val client = httpClient()

        return try {
            val response = client.post("${baseURL}v1/sites/search") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<SiteSearchResponse>()

            response.data

        } catch (e: Exception) {
            println("Error searching for sites: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun findChargerLocations(request: ChargerFindRequest): List<Charger>? {
        val client = httpClient()

        return try {
            val response = client.post("${baseURL}v1/chargers/locations/filter") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<ChargerFindResponse>()

            response.data

        } catch (e: Exception) {
            println("Error finding chargers: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    override suspend fun oneTimePaymentStartTransaction(connectorId: Long,
                                                        request: AddPaymentCardRequest): OneTimePaymentStartTransactionResponse? {
        val client = httpClient()

        return try {
            val response = client.post("${baseURL}v1/chargers/connectors/${connectorId}/remote-operations/one-time-payment-start-transaction") {
                contentType(ContentType.Application.Json)
                headers {
                    append("dmsTicket", ticket)
                }
                setBody(request)
            }.body<OneTimePaymentStartTransactionResponse>()

            response

        } catch (e: Exception) {
            println("Error otp transaction: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun httpClient(): HttpClient {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
        }
        return client
    }
}


@Serializable
data class ServiceAccountResponse(
    val requestId: String,
    val code: String,
    val reason: String,
    val message: String?,
    val messages: List<Message>? = emptyList(),
    val httpStatusCode: Int,
    val count: Int,
    val data: List<ServiceAccountData>
)
@Serializable
data class Message(
    val code: String,
    val reason: String,
    val message: String
)
@Serializable
data class ServiceAccountData(
    val ticket: String
)
