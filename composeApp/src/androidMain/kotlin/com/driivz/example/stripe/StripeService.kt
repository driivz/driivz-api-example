package com.driivz.example.stripe

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.stripe.network.ApiService
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.SetupIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StripeService(
    private val apiService: ApiService,
    private val scope: CoroutineScope
) {
    private var stripe: Stripe? = null

    var configs: StripeSecretResponse? = null
    var pendingCardParams: CardParams? = null

    suspend fun initializeStripe(context: Context) {
        configs = apiService.stripeClientSecret().getOrNull()
        configs?.publicKey?.let {
            PaymentConfiguration.init(context, it)
            stripe = Stripe(context, it)
        }
    }

    fun confirmSetupIntent(
        activity: FragmentActivity,
        card: PaymentMethodCreateParams.Card,
        cardParams: CardParams
    ) {
        val billingDetails: PaymentMethod.BillingDetails = PaymentMethod.BillingDetails.Builder()
            .build()

        pendingCardParams = cardParams

        // Create SetupIntent confirm parameters with the above
        val paymentMethodParams = PaymentMethodCreateParams.create(card, billingDetails)
        val confirmParams: ConfirmSetupIntentParams = ConfirmSetupIntentParams.create(
            paymentMethodParams, configs?.clientSecret.orEmpty())

        stripe?.confirmSetupIntent(activity, confirmParams)
    }

    fun onSetupResult(requestCode: Int, data: Intent?) {
        // Handle the result of stripe.confirmSetupIntent
        stripe?.onSetupResult(requestCode, data, object : ApiResultCallback<SetupIntentResult> {
            override fun onSuccess(result: SetupIntentResult) {

                if (result.intent.status == StripeIntent.Status.Succeeded) {
                    // Setup completed successfully
                    val token = result.intent.paymentMethodId + "|" + configs?.customerId

                    val request = AddPaymentCardRequest(
                        paymentMethodType = pendingCardParams?.brand?.code,
                        nameOnCard = "Name of driver",
                        cardNumber = pendingCardParams?.number(),
                        expiryMonth = pendingCardParams?.expMonth(),
                        expiryYear = pendingCardParams?.expYear(),
                        token = token
                    )
                    scope.launch {
                        apiService.addPaymentMethod(request)
                    }
                } else {
                    if (result.intent.status == StripeIntent.Status.RequiresPaymentMethod) {
                        // Setup failed – allow retrying using a different payment method
                        //applicationDisplayFactory.showErrorToastMessage(R.string.error_type_AUTHORIZATION_FAILED)
                    } else {
                        //applicationDisplayFactory.showErrorToastMessage(R.string.error_type_UNEXPECTED_ERROR)
                    }
                }
            }

            override fun onError(e: Exception) {
                // Setup request failed – allow retrying using the same payment method
                if (e.localizedMessage.isNullOrBlank()) {
                    //applicationDisplayFactory.showErrorToastMessage(R.string.error_type_UNEXPECTED_ERROR)
                } else {
                    //applicationDisplayFactory.showErrorToastMessage(e.localizedMessage)
                }
            }
        })
    }
}