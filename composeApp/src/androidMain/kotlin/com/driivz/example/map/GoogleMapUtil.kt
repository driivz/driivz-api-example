package com.driivz.example.map

import android.content.Context
import android.content.res.Resources
import com.driivz.example.util.dpToPx
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object GoogleMapUtil {

    fun resolveMapPadding(context : Context): Int {
        var padding = 150.dpToPx(context).toInt()
        val maxPadding = context.resources.displayMetrics.widthPixels * 3 / 10
        if (padding > maxPadding)
            padding = maxPadding
        return padding
    }

    fun calculateDistance(from: LatLng, to: LatLng): Double {
        val earthRadius = 6371000 // Earth radius in meters
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun boundsToCenterAndRadius(bounds: LatLngBounds): Pair<LatLng, Double> {
        val centerLat = (bounds.northeast.latitude + bounds.southwest.latitude) / 2
        val centerLng = (bounds.northeast.longitude + bounds.southwest.longitude) / 2
        val center = LatLng(centerLat, centerLng)

        val distanceToNortheast = calculateDistance(center, bounds.northeast)
        val distanceToSouthwest = calculateDistance(center, bounds.southwest)

        val radius = max(distanceToNortheast, distanceToSouthwest)

        return Pair(center, radius)
    }

    fun getDefaultMapPadding(resources: Resources): Int {
        val windowWidth = resources.displayMetrics.widthPixels
        val windowHeight = resources.displayMetrics.heightPixels
        var routeDefaultPadding = 150
        routeDefaultPadding = (routeDefaultPadding * resources.displayMetrics.density).roundToInt()
        var padding = routeDefaultPadding
        val maxHorizontalPadding = windowWidth / 4
        val maxVerticalPadding = windowHeight / 4
        if (padding > maxHorizontalPadding) {
            padding = maxHorizontalPadding
        }
        if (padding > maxVerticalPadding) {
            padding = maxVerticalPadding
        }
        return padding
    }
}