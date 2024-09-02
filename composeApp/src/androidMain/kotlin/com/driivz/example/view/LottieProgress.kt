package com.driivz.example.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.driivz.example.R

@Preview
@Composable
fun PreviewLottiePreview() {
    Column {
        LottieProgress(modifier = Modifier.size(50.dp))
    }
}

@Composable
fun LottieProgress(
    modifier: Modifier = Modifier,
    progressTint: Color = colorResource(id = R.color.loader)
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loader_common))

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = SimpleColorFilter(progressTint.toArgb()),
            keyPath = arrayOf("Shape Layer 2", "**")
        )
    )
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = LottieConstants.IterateForever,
        dynamicProperties = dynamicProperties
    )

}