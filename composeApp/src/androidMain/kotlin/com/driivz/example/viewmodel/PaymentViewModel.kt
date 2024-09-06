package com.driivz.example.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driivz.example.api.StripeSecretResponse
import com.driivz.example.stripe.StripeService
import com.driivz.example.stripe.network.ApiService
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentUiState {
    object Loading : PaymentUiState()
    class Success : PaymentUiState()
    data class Error(val exception: Throwable) : PaymentUiState()
    object Idle : PaymentUiState()
}

class PaymentViewModel(
    private val apiService: ApiService,
    private val stripeService: StripeService
) : ViewModel() {
    var configs: StripeSecretResponse? = null

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun fetchClientSecret(context: Context) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                stripeService.initializeStripe(context)

                _uiState.value = PaymentUiState.Success()
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e)
            }
        }
    }

    fun confirm(
        activity: FragmentActivity,
        card: PaymentMethodCreateParams.Card,
        cardParams: CardParams,
        isOtp: Boolean = false,
        chargerId: Long? = null
    ) {
        stripeService.confirmSetupIntent(activity, card, cardParams, isOtp, chargerId)
    }
}