package com.driivz.example.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.driivz.example.R

@Preview
@Composable
fun PreviewMapProgress(){
    MaterialTheme {
        Surface {
            MapProgress()
        }
    }
}
@Composable
fun MapProgress(modifier: Modifier = Modifier) {

    Surface(
        modifier = modifier
            .shadow(
                shape = CircleShape,
                elevation = 10.dp,
                spotColor = colorResource(id = R.color.shadow),
                ambientColor = colorResource(id = R.color.shadow),
            )
    ) {
        Progress()
    }
}

@Composable
fun Progress(){
    Row(
        modifier = Modifier
            .height(32.dp)
            .padding(end = 12.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        LottieProgress(modifier = Modifier.size(24.dp))
        Text(
            text = "Loading...",
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.body1
        )
    }
}