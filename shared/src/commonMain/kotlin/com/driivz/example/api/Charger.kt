package com.driivz.example.api

import kotlinx.serialization.Serializable


@Serializable
data class Address(
    val address1: String?,
    val address2: String?,
    val city: String?,
    val zipCode: String?,
    val countryCode: String?,
    val stateCode: String?,
    val municipality: String?,
    val region: String?
)

@Serializable
data class Charger(
    val id: Long,
    val name: String?,
    val latitude: Double,
    val longitude: Double,
    val address: Address
)


@Serializable
data class ChargersResponse(val chargers: List<Charger>)



@Serializable
data class OneTimePaymentStartTransaction(
    val status: String,
    val uuid: String
)

@Serializable
data class OneTimePaymentTransactionResponse(val transaction: OneTimePaymentStartTransaction)


fun Charger.toAddress(): String {
    return listOfNotNull(
        this.address.address1.takeIf { it?.isNotBlank() == true },
        this.address.address2.takeIf { it?.isNotBlank() == true },
        this.address.city.takeIf { it?.isNotBlank() == true },
        this.address.zipCode.takeIf { it?.isNotBlank() == true },
        this.address.countryCode.takeIf { it?.isNotBlank() == true },
        this.address.stateCode.takeIf { it?.isNotBlank() == true },
        this.address.municipality.takeIf { it?.isNotBlank() == true },
        this.address.region.takeIf { it?.isNotBlank() == true }
    ).joinToString(", ")
}