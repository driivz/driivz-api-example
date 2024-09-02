package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)
@Serializable
data class LoginResponse(val ticket: String)
