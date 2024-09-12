package com.driivz.example.stripe.network

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.Charger
import com.driivz.example.api.ChargersResponse
import com.driivz.example.api.LoginRequest
import com.driivz.example.api.LoginResponse
import com.driivz.example.api.OneTimePaymentStartTransaction
import com.driivz.example.api.OneTimePaymentTransactionResponse
import com.driivz.example.api.PaymentCard
import com.driivz.example.api.PaymentCardsResponse
import com.driivz.example.api.Site
import com.driivz.example.api.SiteSearchRequest
import com.driivz.example.api.SitesResponse
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.network.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenProvider: TokenProvider
) {

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response: LoginResponse = httpClient.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }.body<LoginResponse>()

            tokenProvider.saveToken(response.ticket)
            Result.success(response.ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentMethods(): Result<List<PaymentCard>> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/payment-methods") {
                header("Authorization", "Bearer ${tokenProvider.getToken()}")
            }
            val paymentMethods = response.body<PaymentCardsResponse>()

            Result.success(paymentMethods.payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPaymentMethod(request: AddPaymentCardRequest): Result<PaymentCard?> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/add-payment") {
                header("Authorization", "Bearer ${tokenProvider.getToken()}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val paymentMethods = response.body<PaymentCardsResponse>()

            Result.success(paymentMethods.payments.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchSites(request: SiteSearchRequest): Result<List<Site>?> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/sites") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val sitesResponse = response.body<SitesResponse>()

            Result.success(sitesResponse.sites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findChargers(siteId: Long): Result<List<Charger>?> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/site/${siteId}/chargers") {
                contentType(ContentType.Application.Json)
            }
            val chargersResponse = response.body<ChargersResponse>()

            Result.success(chargersResponse.chargers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stripeClientSecret(): Result<StripeSecretResponse> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/stripe/client-secret")
            val paymentMethods = response.body<StripeSecretResponse>()

            Result.success(paymentMethods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun oneTimePaymentStartTransaction(chargerId:Long, request: AddPaymentCardRequest):
            Result<OneTimePaymentStartTransaction?> {

        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/charger/otp/${chargerId}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val transactionResponse = response.body<OneTimePaymentTransactionResponse>()
            if (transactionResponse.transaction != null) {
                Result.success(transactionResponse.transaction)
            } else {
                Result.failure(Throwable(transactionResponse.error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}