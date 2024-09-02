package com.driivz.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driivz.example.api.PaymentCard
import com.driivz.example.stripe.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentListState {
    object Idle : PaymentListState()
    object Loading : PaymentListState()
    data class Success(val paymentMethods: List<PaymentCard>) : PaymentListState()
    data class Error(val message: String) : PaymentListState()
}

class PaymentListViewModel(private val apiService: ApiService) : ViewModel() {

    private val _paymentListState = MutableStateFlow<PaymentListState>(PaymentListState.Idle)
    val paymentListState: StateFlow<PaymentListState> = _paymentListState

    fun fetchPaymentMethods() {
        _paymentListState.value = PaymentListState.Loading

        viewModelScope.launch {
            val result = apiService.getPaymentMethods()
            _paymentListState.value = when {
                result.isSuccess -> PaymentListState.Success(result.getOrNull() ?: emptyList())
                result.isFailure -> PaymentListState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                else -> PaymentListState.Idle
            }
        }
    }
}