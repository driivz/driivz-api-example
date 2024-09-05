package com.driivz.example.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.driivz.example.R

@Preview
@Composable
fun PreviewCluster() {
    Cluster(clusterSize = 11125, isEnlarged = false)
}

@Preview
@Composable
fun PreviewLargeCluster() {
    Cluster(clusterSize = 11125, isEnlarged = true)
}

@Composable
fun Cluster(clusterSize: Int, isEnlarged: Boolean = false) {
    val clusterBackground = colorResource(id = R.color.map_cluster_background)

    val enlargerSizeAdditionalSize = 6.dp
    val textSize =
        with(LocalDensity.current) {
            if (isEnlarged)
                if (clusterSize > 9999)
                    10.dp.toSp()
                else if (clusterSize > 999)
                    14.dp.toSp()
                else 18.dp.toSp()
            else {
                if (clusterSize > 9999)
                    9.dp.toSp()
                else if (clusterSize > 999)
                    10.dp.toSp()
                else 12.dp.toSp()
            }
        }

    Box(
        Modifier
            .border(width = 2.dp, color = clusterBackground, CircleShape)
            .padding(3.dp)
            .size(
                32.dp + if (isEnlarged) {
                    enlargerSizeAdditionalSize
                } else 0.dp
            ), contentAlignment = Alignment.Center
    ) {
        Surface(
            Modifier
                .size(
                    28.dp + if (isEnlarged) {
                        enlargerSizeAdditionalSize
                    } else 0.dp
                ),
            shape = CircleShape,
            color = clusterBackground,
            contentColor = colorResource(id = R.color.map_cluster_text)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    clusterSize.toString(),
                    style = MaterialTheme.typography.body1,
                    color = colorResource(id = R.color.map_cluster_text),
                    fontSize = textSize
                )
            }
        }
    }

}