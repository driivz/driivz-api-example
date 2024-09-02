package com.driivz.example.stripe.network

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.LoginRequest
import com.driivz.example.api.LoginResponse
import com.driivz.example.api.PaymentCard
import com.driivz.example.api.PaymentCardsResponse
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.network.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

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

    suspend fun stripeClientSecret(): Result<StripeSecretResponse> {
        return try {
            val response: HttpResponse = httpClient.get("$baseUrl/stripe/client-secret")
            val paymentMethods = response.body<StripeSecretResponse>()

            Result.success(paymentMethods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}