package com.driivz.example.stripe.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.driivz.example.view.MapProgress
import com.driivz.example.viewmodel.MapUiState
import com.driivz.example.viewmodel.MapViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = getViewModel()
) {
    val mapUiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.zIndex(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                if (mapUiState is MapUiState.Loading) MapProgress(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}