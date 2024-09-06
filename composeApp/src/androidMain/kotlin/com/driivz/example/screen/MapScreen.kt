package com.driivz.example.stripe.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.driivz.example.map.DriivzClustering
import com.driivz.example.map.GoogleMapUtil
import com.driivz.example.map.ManySitesClusterItem
import com.driivz.example.map.MapLocationSource
import com.driivz.example.map.SiteClusterItem
import com.driivz.example.map.SiteMapPin
import com.driivz.example.view.Cluster
import com.driivz.example.view.MapProgress
import com.driivz.example.viewmodel.MapUiState
import com.driivz.example.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


private const val MAP_ANIMATION_DURATION = 1600
private const val LOCATION_ZOOM = 14f

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = getViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val locationSource = MapLocationSource()

    val mapUiState by viewModel.uiState.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()

    var isMapLoaded: Boolean by remember {
        mutableStateOf(false)
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                isIndoorEnabled = false,
                isTrafficEnabled = false
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                indoorLevelPickerEnabled = false
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        val lastCameraPosition = viewModel.lastCameraPosition
        position =
            if (searchResult == viewModel.lastSearchResultInMap && lastCameraPosition != null) {
                lastCameraPosition
            } else {
                CameraPosition.fromLatLngZoom(
                    searchResult.location,
                    LOCATION_ZOOM
                )
            }
    }

    val context = LocalContext.current

    val throttleFlow = remember { MutableSharedFlow<VisibleRegion>(extraBufferCapacity = 1) }
    LaunchedEffect(Unit) {
        throttleFlow
            .debounce(600L)
            .collect { visibleRegion ->
                viewModel.fetchItemsByVisibleRegion(visibleRegion)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(top = 40.dp)
    ) {
        Column(modifier = Modifier.zIndex(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                if (mapUiState is MapUiState.Loading)
                    MapProgress(text = "Loading...",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)

                ) else if (mapUiState is MapUiState.Error)
                    MapProgress(text = "Error!",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp))
            }
        }

        GoogleMap(modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = {
                isMapLoaded = true
            }) {

            LaunchedEffect(searchResult, isMapLoaded) {

                if (searchResult == viewModel.lastSearchResultInMap
                    || !isMapLoaded
                ) {
                    return@LaunchedEffect
                }

                delay(200L)
                // because sometimes we need to wait little bit more when map is Loading,
                // when it returns loaded and still not rly working
                viewModel.lastSearchResultInMap = searchResult

                if (searchResult.bounds?.northeast != null) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(
                            searchResult.bounds!!,
                            GoogleMapUtil.resolveMapPadding(context)
                        ), MAP_ANIMATION_DURATION
                    )
                } else {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            searchResult.location,
                            LOCATION_ZOOM
                        ), MAP_ANIMATION_DURATION
                    )
                }
            }

            if (isMapLoaded) {
                DriivzClustering(
                    items = (mapUiState as? MapUiState.Success)?.items.orEmpty(),
                    shouldRenderAsCluster = true,
                    onClusterClick = {
                        coroutineScope.launch {
                            if (it.items.first() is ManySitesClusterItem) {
                                onServerClusterOrMarkerClick(navController, it.items.first())
                            } else {
                                val latLngBuilder = LatLngBounds.Builder()
                                it.items?.forEach {
                                    latLngBuilder.include(
                                        LatLng(
                                            it.position.latitude, it.position.longitude
                                        )
                                    )
                                }
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(
                                        latLngBuilder.build(),
                                        GoogleMapUtil.getDefaultMapPadding(context.resources)
                                    )
                                )
                            }
                        }
                        true
                    },
                    onClusterItemClick = { clusterItem ->
                        coroutineScope.launch {
                            onServerClusterOrMarkerClick(navController, clusterItem)
                        }
                        true
                    },
                    clusterContent = { cluster ->
                        Cluster(clusterSize = cluster.size, isEnlarged = false)
                    },
                    clusterItemContent = { siteMapClusterItem ->
                        when (siteMapClusterItem) {
                            is ManySitesClusterItem -> {
                                Cluster(
                                    clusterSize = siteMapClusterItem.sitesIds.size,
                                    isEnlarged = false
                                )
                            }

                            is SiteClusterItem -> {
                                SiteMapPin(siteMapClusterItem)
                            }
                        }
                    },
                )

                LaunchedEffect(cameraPositionState.isMoving) {

                    if (!cameraPositionState.isMoving) {
                        viewModel.lastCameraPosition = cameraPositionState.position
                    }

                    cameraPositionState.projection?.visibleRegion?.let {
                        throttleFlow.tryEmit(it)
                    }
                }
            }
        }
    }
}

fun onServerClusterOrMarkerClick(
    navController: NavController,
    clusterItem: ClusterItem?
) {
    when (clusterItem) {
        is ManySitesClusterItem -> {
            navController.navigate("chargersList/${clusterItem.sitesIds.first()}")
        }

        is SiteClusterItem -> {
            navController.navigate("chargersList/${clusterItem.id}")
        }
    }
}