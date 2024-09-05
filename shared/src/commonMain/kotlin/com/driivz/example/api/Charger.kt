package com.driivz.example.api

import kotlinx.serialization.Serializable


@Serializable
data class Address(
    val address1: String,
    val address2: String,
    val city: String,
    val zipCode: String,
    val countryCode: String,
    val stateCode: String,
    val municipality: String,
    val region: String
)

@Serializable
data class Charger(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: Address
)

@Serializable
data class ChargerFindRequest (
    val ids: List<Int>
)

@Serializable
data class ChargersResponse(val chargers: List<Charger>)


fun Charger.toAddress(): String {
    return listOfNotNull(
        this.address.address1.takeIf { it.isNotBlank() },
        this.address.address2.takeIf { it.isNotBlank() },
        this.address.city.takeIf { it.isNotBlank() },
        this.address.zipCode.takeIf { it.isNotBlank() },
        this.address.countryCode.takeIf { it.isNotBlank() },
        this.address.stateCode.takeIf { it.isNotBlank() },
        this.address.municipality.takeIf { it.isNotBlank() },
        this.address.region.takeIf { it.isNotBlank() }
    ).joinToString(", ")
}