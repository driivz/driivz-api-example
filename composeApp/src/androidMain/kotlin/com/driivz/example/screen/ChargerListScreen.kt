package com.driivz.example.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.driivz.example.api.Charger
import com.driivz.example.api.toAddress
import com.driivz.example.viewmodel.ChargerListState
import com.driivz.example.viewmodel.ChargerListViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ChargerListScreen(
    navController: NavController,
    chargerListViewModel: ChargerListViewModel = getViewModel()
) {
    val chargerState by chargerListViewModel.chargerListState.collectAsState()

    LaunchedEffect(Unit) {
        chargerListViewModel.fetchPaymentMethods()
    }

    when (chargerState) {
        is ChargerListState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ChargerListState.Success -> {
            val chargers = (chargerState as ChargerListState.Success).chargers
            ChargersList(chargers)
        }
        is ChargerListState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = (chargerState as ChargerListState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
        }
        else -> Unit
    }
}

@Composable
fun ChargersList(
    chargers: List<Charger>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(chargers) { charger ->
                ChargerItem(charger)
            }
        }
    }
}

@Composable
fun ChargerItem(charger: Charger) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = charger.name)
            Text(text = charger.toAddress())
        }
    }
}