package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationRequest(
    val names: List<String>
)

@Serializable
data class ConfigurationResponse(
    val requestId: String,
    val code: String,
    val reason: String,
    val message: String,
    val httpStatusCode: Int,
    val count: Int,
    val data: List<Configuration>
)

@Serializable
data class Configuration(
    val id: Int,
    val name: String,
    val value: String,
    val description: String,
    val allowedUserOperation: String
)
