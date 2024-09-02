package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class StripeSecretResponse(
    val publicKey: String?,
    val clientSecret: String,
    val customerId: String,
    val ephermalKey: String
)