package com.driivz.example.api

import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val code: String,
    val reason: String,
    val message: String
)

@Serializable
data class PaymentCard(
    val id: Int,
    val primary: Boolean?,
    val createdOn: Long?,
    val type: String?,
    val name: String?,
    val cardNumber: String,
    val bic: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?,
    val expired: Boolean?,
    val deleted: Boolean?
)

@Serializable
data class PaymentCardsResponse(val payments: List<PaymentCard>)

@Serializable
data class AddPaymentCardRequest(
    val customerId: String? = null,
    var paymentMethodType: String?,
    var tokenType: String? = null,
    val nameOnCard: String?,
    var cardNumber: String?,
    val bic: String? = null,
    var expiryMonth: Int?,
    var expiryYear: Int?,
    var stripeAuthorizedToken: StripeAuthorizedToken?
)

@Serializable
data class StripeAuthorizedToken(
    val customer: String?,
    val paymentMethod: String?
)