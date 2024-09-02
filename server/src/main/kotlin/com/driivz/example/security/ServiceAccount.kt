package com.driivz.example.security

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.PaymentCard

interface ServiceAccount {
    val ticket: String
    val expirationTime: Long

    fun isTokenValid(): Boolean {
        return System.currentTimeMillis() < expirationTime
    }

    suspend fun login(): Boolean

    suspend fun configurationValues(paramKeys: List<String>): List<String>?

    suspend fun configurationValue(paramKey: String): String?

    suspend fun findCustomerAccountNumber(userName: String): Int?

    suspend fun loginAsCustomer(accountNumber: Int): String?

    suspend fun paymentMethods(accountNumber: Int): List<PaymentCard>?

    suspend fun addPayment(request: AddPaymentCardRequest): PaymentCard?
}