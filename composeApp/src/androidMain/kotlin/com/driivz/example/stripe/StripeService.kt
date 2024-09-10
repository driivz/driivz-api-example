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
    var pendingChargerIdTransaction: Long? = null

    var pendingOnSuccess: ((String) -> Unit)? = null
    var pendingOnError: ((String) -> Unit)? = null

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
        cardParams: CardParams,
        isOtp: Boolean = false,
        chargerId: Long? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val billingDetails: PaymentMethod.BillingDetails = PaymentMethod.BillingDetails.Builder()
            .build()

        pendingCardParams = cardParams
        if (isOtp) {
            pendingChargerIdTransaction = chargerId
        }

        pendingOnSuccess = onSuccess
        pendingOnError = onError

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
                    val isOtp = pendingChargerIdTransaction != null

                    // Setup completed successfully
                    val token = result.intent.paymentMethodId + "|" + configs?.customerId
                    val request = AddPaymentCardRequest(
                        paymentMethodType = pendingCardParams?.brand?.code,
                        nameOnCard = if (isOtp) "OTP driver" else "Name of driver",
                        cardNumber = pendingCardParams?.number(),
                        expiryMonth = pendingCardParams?.expMonth(),
                        expiryYear = pendingCardParams?.expYear(),
                        token = token
                    )

                    scope.launch {
                        try {
                            if (isOtp) {
                                apiService.oneTimePaymentStartTransaction(pendingChargerIdTransaction!!, request)
                            } else {
                                apiService.addPaymentMethod(request)
                            }
                            pendingOnSuccess?.invoke("Payment card setup successful!")
                        } catch (e: Exception) {
                            pendingOnError?.invoke("Failed to save payment method: ${e.localizedMessage}")
                        }
                    }
                } else {
                    if (result.intent.status == StripeIntent.Status.RequiresPaymentMethod) {
                        pendingOnError?.invoke("Setup failed – retry using a different payment method")
                    } else {
                        pendingOnError?.invoke("Setup failed – unexpected error")
                    }
                }
            }

            override fun onError(e: Exception) {
                // Setup request failed – allow retrying using the same payment method
                if (e.localizedMessage.isNullOrBlank()) {
                    pendingOnError?.invoke("Setup failed – unexpected error")
                } else {
                    pendingOnError?.invoke("Failed to save payment method: ${e.localizedMessage}")
                }
            }
        })
    }
}