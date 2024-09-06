package com.driivz.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driivz.example.api.Charger
import com.driivz.example.stripe.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChargerListState {
    object Idle : ChargerListState()
    object Loading : ChargerListState()
    data class Success(val chargers: List<Charger>) : ChargerListState()
    data class Error(val message: String) : ChargerListState()
}

class ChargerListViewModel(private val apiService: ApiService) : ViewModel() {

    private val _chargerListState = MutableStateFlow<ChargerListState>(ChargerListState.Idle)
    val chargerListState: StateFlow<ChargerListState> = _chargerListState

    fun fetchSiteChargers(siteId: Long) {
        _chargerListState.value = ChargerListState.Loading

        viewModelScope.launch {
            val result = apiService.findChargers(siteId)
            _chargerListState.value = when {
                result.isSuccess -> ChargerListState.Success(result.getOrNull() ?: emptyList())
                result.isFailure -> ChargerListState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                else -> ChargerListState.Idle
            }
        }
    }
}