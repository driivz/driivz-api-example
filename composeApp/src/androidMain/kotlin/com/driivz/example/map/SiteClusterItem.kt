package com.driivz.example.map

import com.driivz.example.api.Site
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class SiteClusterItem(private val site: Site) : ClusterItem {

    val itemPosition: LatLng = LatLng(site.latitude, site.longitude)
    val id: Long = site.id.toLong()

    override fun getZIndex(): Float = 20f
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SiteClusterItem) return false
        if (!super.equals(other)) return false

        if (site != other.site) return false
        if (itemPosition != other.itemPosition) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + site.hashCode()
        result = 31 * result + itemPosition.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun getPosition(): LatLng {
        return itemPosition
    }

    override fun getTitle(): String? {
        return site.displayName
    }

    override fun getSnippet(): String? {
        return site.name
    }
}
