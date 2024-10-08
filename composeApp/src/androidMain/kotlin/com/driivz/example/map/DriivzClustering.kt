package com.driivz.example.map

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.InputHandler
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.currentCameraPositionState
import com.google.maps.android.compose.rememberComposeUiViewRenderer
import com.google.maps.android.compose.rememberReattachClickListenersHandle
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
@Composable
@GoogleMapComposable
@MapsComposeExperimentalApi
public fun <T : ClusterItem> DriivzClustering(
    items: Collection<T>,
    shouldRenderAsCluster: Boolean,
    onClusterClick: (Cluster<T>) -> Boolean = { false },
    onClusterItemClick: (T) -> Boolean = { false },
    onClusterItemInfoWindowClick: (T) -> Unit = { },
    onClusterItemInfoWindowLongClick: (T) -> Unit = { },
    clusterContent: @[UiComposable Composable] ((Cluster<T>) -> Unit)? = null,
    clusterItemContent: @[UiComposable Composable] ((T) -> Unit)? = null,
    clusterRenderer: ClusterRenderer<T>? = null
) {

    val clusterManager =
        rememberClusterManager(
            clusterContent,
            clusterItemContent,
            shouldRenderAsCluster,
            clusterRenderer
        ) ?: return

    ResetMapListeners(clusterManager)
    SideEffect {
        clusterManager.setOnClusterClickListener(onClusterClick)
        clusterManager.setOnClusterItemClickListener(onClusterItemClick)
        clusterManager.setOnClusterItemInfoWindowClickListener(onClusterItemInfoWindowClick)
        clusterManager.setOnClusterItemInfoWindowLongClickListener(onClusterItemInfoWindowLongClick)
    }
    InputHandler(
        onMarkerClick = clusterManager.markerManager::onMarkerClick,
        onInfoWindowClick = clusterManager.markerManager::onInfoWindowClick,
        onInfoWindowLongClick = clusterManager.markerManager::onInfoWindowLongClick,
        onMarkerDrag = clusterManager.markerManager::onMarkerDrag,
        onMarkerDragEnd = clusterManager.markerManager::onMarkerDragEnd,
        onMarkerDragStart = clusterManager.markerManager::onMarkerDragStart,
    )
    val cameraPositionState = currentCameraPositionState
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .collect { isMoving ->
                if (!isMoving) {
                    clusterManager.onCameraIdle()
                }
            }
    }
    val itemsState = rememberUpdatedState(items)
    LaunchedEffect(itemsState) {
        snapshotFlow { itemsState.value.toList() }
            .collect { items ->
                clusterManager.clearItems()
                clusterManager.addItems(items)
                clusterManager.cluster()
            }
    }
}

@SuppressLint("RestrictedApi")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun <T : ClusterItem> rememberClusterManager(
    clusterContent: @Composable ((Cluster<T>) -> Unit)?,
    clusterItemContent: @Composable ((T) -> Unit)?,
    shouldRenderAsCluster: Boolean,
    clusterRenderer: ClusterRenderer<T>? = null
): ClusterManager<T>? {
    val clusterContentState = rememberUpdatedState(clusterContent)
    val clusterItemContentState = rememberUpdatedState(clusterItemContent)
    val context = LocalContext.current
    val viewRendererState = rememberUpdatedState(rememberComposeUiViewRenderer())
    val clusterManagerState: MutableState<ClusterManager<T>?> = remember { mutableStateOf(null) }
    MapEffect(context) { map ->
        val clusterManager = ClusterManager<T>(context, map)
        clusterManager.algorithm =
            NonHierarchicalDistanceBasedAlgorithm<T>().apply {
                maxDistanceBetweenClusteredItems = 50
            }
        launch {
            snapshotFlow {
                clusterContentState.value != null || clusterItemContentState.value != null
            }
                .collect { hasCustomContent ->
                    val renderer = clusterRenderer
                        ?: if (hasCustomContent) {
                            DriivzClusterRenderer(
                                context,
                                scope = this,
                                map,
                                clusterManager,
                                shouldRenderAsCluster,
                                viewRendererState,
                                clusterContentState,
                                clusterItemContentState,
                            )
                        } else {
                            DefaultClusterRenderer(context, map, clusterManager)
                        }
                    clusterManager.renderer = renderer
                }
        }

        clusterManagerState.value = clusterManager
    }
    return clusterManagerState.value
}

/**
 * This is a hack.
 * [ClusterManager] instantiates a [MarkerManager], which posts a runnable to the UI thread that
 * overwrites a bunch of [GoogleMap]'s listeners. Many Maps composables rely on those listeners
 * being set by [com.google.maps.android.compose.MapApplier].
 * This posts _another_ runnable which effectively undoes that, signaling MapApplier to set the
 * listeners again.
 * This is heavily coupled to implementation details of [MarkerManager].
 */
@SuppressLint("RestrictedApi")
@Composable
private fun ResetMapListeners(
    clusterManager: ClusterManager<*>,
) {
    val reattach = rememberReattachClickListenersHandle()
    LaunchedEffect(clusterManager, reattach) {
        Handler(Looper.getMainLooper()).post {
            reattach()
        }
    }
}