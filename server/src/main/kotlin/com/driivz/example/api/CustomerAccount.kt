package com.driivz.example.api

import kotlinx.serialization.Serializable

@Serializable
data class CustomerLoginRequest(
    val accountNumber: Int
)

@Serializable
data class CustomerAccountFilterRequest(
    val email: String,
    val isOtp: Boolean
)

@Serializable
data class CustomerAccountFilterResponse(
    val requestId: String,
    val code: String,
    val reason: String,
    val message: String,
    val httpStatusCode: Int,
    val count: Int,
    val data: List<AccountData>
)

@Serializable
data class AccountData(
    val id: Int?,
    val accountNumber: Int,
    val firstName: String?,
    val lastName: String?,
    val gender: String?,
    val accountType: String?,
    val status: String?,
    val email: String?,
    val mobilePhone: String?,
    val familyId: Int?,
    val externalId: String?,
    val groupName: String?,
    val shippingAddress: Address?,
    val billingAddress: Address?,
    val b2b: B2BInfo?
)

@Serializable
data class Address(
    val address1: String?,
    val address2: String?,
    val city: String?,
    val zipCode: String?,
    val countryCode: String?,
    val stateCode: String?,
    val zoneId: String?,
    val market: String?,
    val municipality: String?,
    val region: String?
)

@Serializable
data class B2BInfo(
    val taxNumber: String,
    val vatNumber: String,
    val identificationNumber: String,
    val taxOffice: String,
    val profession: String,
    val customerName: String
)
