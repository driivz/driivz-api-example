package com.driivz.example.map

import com.driivz.example.api.Site
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


fun List<Site>.toClusterItems(): List<ClusterItem> {
    val clusterItems = ArrayList<ClusterItem>()
    return this.let {
        val groupedByPosition = it.groupBy { LatLng(it.latitude, it.longitude) }
        groupedByPosition.entries.forEach { entry ->
            val siteIds = entry.value.map { it.id.toLong() }
            if (siteIds.size > 1) {
                entry.value.forEach {
                    clusterItems.add(ManySitesClusterItem(it, siteIds))
                }
            } else {
                val siteMapData = entry.value.first()
                clusterItems.add(SiteClusterItem(siteMapData))
            }
        }
        clusterItems
    }
}