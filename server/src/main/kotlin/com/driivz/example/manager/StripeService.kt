package com.driivz.example.manager

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.toPaymentMethodType
import com.stripe.model.PaymentMethod
import com.stripe.model.SetupIntent
import com.stripe.net.RequestOptions
import com.stripe.param.PaymentMethodListParams
import com.stripe.param.SetupIntentCreateParams
import io.ktor.server.config.HoconApplicationConfig

class StripeService(private val config: HoconApplicationConfig) {

    fun authorizeStripePayment(request: AddPaymentCardRequest) {
        val stripePrivateKey = config.property("ktor.stripe.privateKey").getString()

        val tokenSplit = request.token?.split("|")

        val paymentMethodData = tokenSplit?.getOrNull(0).orEmpty()
        val customerId = tokenSplit?.getOrNull(1).orEmpty()

        val paymentMethodListParams = PaymentMethodListParams.builder()
            .setCustomer(customerId)
            .setType(PaymentMethodListParams.Type.CARD)
            .build()

        val requestOptions = RequestOptions.builder().setApiKey(stripePrivateKey).build()
        val paymentMethods = PaymentMethod.list(paymentMethodListParams, requestOptions)

        if (paymentMethods != null) {
            paymentMethods.data.forEach { println("Stripe payment card authorization -payment method: ${it.type}") }

            val cardParams = SetupIntentCreateParams.PaymentMethodOptions.Card.builder()
                .setRequestThreeDSecure(SetupIntentCreateParams.PaymentMethodOptions.Card.RequestThreeDSecure.ANY)
                .build()
            val paymentMethodOptions = SetupIntentCreateParams.PaymentMethodOptions.builder()
                .setCard(cardParams)
                .build()

            val setupIntentCreateParams = SetupIntentCreateParams.builder()
                .setPaymentMethodOptions(paymentMethodOptions)
                .setPaymentMethod(paymentMethodData)
                .setCustomer(customerId)
                .setConfirm(true)
                .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                .build()

            val setupIntent = SetupIntent.create(setupIntentCreateParams, requestOptions)
            val paymentMethod = PaymentMethod.retrieve(setupIntentCreateParams.paymentMethod)

            request.expiryMonth = paymentMethod.card.expMonth.toInt()
            request.expiryYear = paymentMethod.card.expYear.toInt()
            request.paymentMethodType = paymentMethod.card.brand.toPaymentMethodType()
            request.cardNumber = "*".repeat(8) + paymentMethod.card.last4
            request.token = "${setupIntent.paymentMethod}|${setupIntent.customer}"
            request.tokenType = "AUTHORIZED_TOKEN"
        }
    }
}