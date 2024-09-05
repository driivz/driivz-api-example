package com.driivz.example.api

import kotlinx.serialization.Serializable


@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val radius: Double
)

@Serializable
data class SiteSearchRequest(
    val geoLocation: GeoLocation
)

@Serializable
data class SitesResponse(val sites: List<Site>)

@Serializable
data class Site(
    val id: Long,
    val name: String?,
    val displayName: String?,
    val latitude: Double,
    val longitude: Double,
    val chargerIds: List<Int>?
)