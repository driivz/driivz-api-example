package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCardResponse(
    val requestId: String?,
    val code: String?,
    val reason: String?,
    val message: String?,
    val messages: List<Message>?,
    val httpStatusCode: Int?,
    val count: Int?,
    val data: List<PaymentCard>?
)

@Serializable
data class Message(
    val code: String?,
    val reason: String?,
    val message: String?
)

@Serializable
data class AddPaymentCardResponse(
    val requestId: String,
    val code: String,
    val reason: String,
    val message: String,
    val messages: List<Message>,
    val httpStatusCode: Int,
    val count: Int,
    val data: List<PaymentCard>
)

fun String.toPaymentMethodType(): String? {
    return when {
        this.isBlank() -> null
        this.lowercase().startsWith("american express") || this.lowercase().startsWith("amex") -> "AMERICAN_EXPRESS"
        this.lowercase() == "bancontact" -> "BANKCARD"
        this.lowercase() == "maestro" -> "MAESTRO"
        this.lowercase().startsWith("mastercard") || this.lowercase().startsWith("mc") -> "MASTERCARD"
        this.lowercase().startsWith("visa") || this.lowercase().startsWith("electron") || this.lowercase() == "vpay" -> "VISA"
        this.lowercase() == "diners" -> "DINERS"
        else -> null
    }
}