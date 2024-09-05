package com.driivz.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driivz.example.api.GeoLocation
import com.driivz.example.api.Site
import com.driivz.example.api.SiteSearchRequest
import com.driivz.example.map.GoogleMapUtil.boundsToCenterAndRadius
import com.driivz.example.map.toClusterItems
import com.driivz.example.stripe.network.ApiService
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.clustering.ClusterItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class SearchResult(
    var location: LatLng,
    var bounds: LatLngBounds? = null)

sealed class MapUiState {
    object Loading : MapUiState()
    class Success(val items: Collection<ClusterItem>?) : MapUiState()
    data class Error(val exception: String) : MapUiState()
    object Idle : MapUiState()
}

class MapViewModel(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState

    protected val _currentVisibleRegion = MutableStateFlow<VisibleRegion?>(null)

    var lastCameraPosition: CameraPosition? = null
    var lastSearchResultInMap: SearchResult? = null

    // Budapest, Hungary
    private val _searchResult = MutableStateFlow(SearchResult(LatLng(47.4810954, 18.9654984)).apply {
        bounds = LatLngBounds(
            LatLng(47.27726339447944, 18.634888265539935),
            LatLng(47.68470427536582, 19.625717733313373)
        )
    })

    private var lastSearchResult: SearchResult? = null //remember things for searchButton
    val searchResult = _searchResult.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun fetchItemsByVisibleRegion(
        visibleRegion: VisibleRegion,
        delay: Long = 0L
    ) {
        viewModelScope.launch {
            delay(delay)
            updateItemsInBounds(visibleRegion.latLngBounds)
        }
    }

    private fun updateItemsInBounds(bounds: LatLngBounds) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading

            val (center, radius) = boundsToCenterAndRadius(bounds)

            val request = SiteSearchRequest(GeoLocation(center.latitude, center.longitude, radius))
            val result = apiService.searchSites(request)
            _uiState.value = when {
                    result.isSuccess -> {
                        MapUiState.Success(result.getOrNull()?.toClusterItems())
                    }
                    result.isFailure -> MapUiState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
                    else -> MapUiState.Idle
                }
        }
    }
}