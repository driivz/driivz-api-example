package com.driivz.example.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.driivz.example.R
import com.driivz.example.api.Site

@Preview
@Composable
fun PreviewMapPin() {
    Surface {
        Row {
            SiteMapPin(
                clusterItem = SiteClusterItem(
                    Site(0L, "", "", 0.0, 0.0, emptyList())
                )
            )
        }
    }
}

@Composable
fun SiteMapPin(clusterItem: SiteClusterItem) {
    val pinText = ""
    val pinWidth = 33.dp
    val pinHeight = 50.dp
    val density = LocalDensity.current

    val textSize = with(density) { 18.dp.toSp() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box {
            Box(
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Image(
                    modifier = Modifier.size(width = pinWidth, height = pinHeight),
                    painter = painterResource(id = R.drawable.map_pin),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        colorResource(id = R.color.map_cluster_background)
                    )
                )

                Image(
                    modifier = Modifier.size(width = pinWidth, height = pinHeight),
                    painter = painterResource(id = R.drawable.map_circle_background_pin),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier
                        .padding(
                            top = 6.5.dp
                        )
                        .align(Alignment.TopCenter),
                    text = pinText,
                    color = colorResource(id = R.color.map_cluster_background),
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize
                )
            }
        }
    }
}
