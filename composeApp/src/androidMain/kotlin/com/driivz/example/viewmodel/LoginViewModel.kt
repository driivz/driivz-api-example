package com.driivz.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driivz.example.stripe.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    class Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val apiService: ApiService) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = apiService.login(username, password)
            _loginState.value = when {
                result.isSuccess -> LoginState.Success()
                result.isFailure -> LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                else -> LoginState.Idle
            }
        }
    }
}