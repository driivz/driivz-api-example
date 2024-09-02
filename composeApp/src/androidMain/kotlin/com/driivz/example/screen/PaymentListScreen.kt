package com.driivz.example.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
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
import com.driivz.example.api.PaymentCard
import com.driivz.example.viewmodel.PaymentListState
import com.driivz.example.viewmodel.PaymentListViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun PaymentListScreen(
    navController: NavController,
    paymentListViewModel: PaymentListViewModel = getViewModel()
) {
    val paymentState by paymentListViewModel.paymentListState.collectAsState()

    LaunchedEffect(Unit) {
        paymentListViewModel.fetchPaymentMethods()
    }

    when (paymentState) {
        is PaymentListState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is PaymentListState.Success -> {
            val paymentMethods = (paymentState as PaymentListState.Success).paymentMethods
            PaymentMethodsList(navController, paymentMethods)
        }
        is PaymentListState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = (paymentState as PaymentListState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
        }
        else -> Unit
    }
}

@Composable
fun PaymentMethodsList(
    navController: NavController,
    paymentMethods: List<PaymentCard>
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
            items(paymentMethods) { paymentMethod ->
                PaymentMethodItem(paymentMethod)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("payment") }) {
            Text("Add payment")
        }
    }
}

@Composable
fun PaymentMethodItem(paymentMethod: PaymentCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            paymentMethod.name?.let { Text(text = it) }
            Text(text = paymentMethod.cardNumber)
            Text(text = "Expiry: ${paymentMethod.expiryMonth}/${paymentMethod.expiryYear}")
            if (paymentMethod.primary == true) {
                Text(text = "Primary", color = MaterialTheme.colors.primary)
            }
        }
    }
}