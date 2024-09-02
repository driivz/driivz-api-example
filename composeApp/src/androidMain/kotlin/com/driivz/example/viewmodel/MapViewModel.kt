package com.driivz.example.viewmodel

import androidx.lifecycle.ViewModel
import com.driivz.example.stripe.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class MapUiState {
    object Loading : MapUiState()
    class Success : MapUiState()
    data class Error(val exception: Throwable) : MapUiState()
    object Idle : MapUiState()
}

class MapViewModel(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState


}