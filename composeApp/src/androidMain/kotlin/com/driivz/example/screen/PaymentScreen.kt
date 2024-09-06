package com.driivz.example.stripe.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.stripe.StripeService
import com.driivz.example.stripe.expMonth
import com.driivz.example.stripe.expYear
import com.driivz.example.stripe.name
import com.driivz.example.stripe.number
import com.driivz.example.viewmodel.PaymentUiState
import com.driivz.example.viewmodel.PaymentViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.SetupIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import com.stripe.android.payments.paymentlauncher.rememberPaymentLauncher
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.stripe.android.utils.rememberActivity
import com.stripe.android.utils.rememberActivityOrNull
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.coroutineScope
import org.koin.androidx.compose.getViewModel

@SuppressLint("RestrictedApi")
@Composable
fun PaymentScreen(
    navController: NavController,
    isOtp: Boolean = false,
    chargerId: Long? = null,
    paymentViewModel: PaymentViewModel = getViewModel()
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cardInputWidgetRef = remember { mutableStateOf<CardInputWidget?>(null) }

    val activity = rememberActivity {
        "PaymentScreen must be created in the context of an Activity"
    }

    LaunchedEffect(Unit) {
        paymentViewModel.fetchClientSecret(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is PaymentUiState.Loading -> {
                CircularProgressIndicator()
            }
            is PaymentUiState.Success -> {

            }
            is PaymentUiState.Error -> {
                val exception = (uiState as PaymentUiState.Error).exception
                Text("Payment failed: ${exception.message}")
            }
            is PaymentUiState.Idle -> {
                // Show idle state or initial UI
            }
        }

        AndroidView(
            factory = { ctx ->
                CardInputWidget(ctx).also { cardInputWidgetRef.value = it }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val cardInputWidget = cardInputWidgetRef.value
            val card = cardInputWidget?.paymentMethodCard
            val cardParams = cardInputWidget?.cardParams

            if (card != null && cardParams != null) {
                paymentViewModel.confirm(activity as FragmentActivity, card, cardParams, isOtp, chargerId)
            } else {
                // Handle error
                //viewModel.onPaymentFailed(Throwable("Invalid card details"))
            }
        }) {
            Text("Pay Now")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("main") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Main")
        }
    }
}
