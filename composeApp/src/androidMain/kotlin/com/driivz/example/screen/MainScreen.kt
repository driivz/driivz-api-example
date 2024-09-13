package com.driivz.example.stripe.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(navController: NavController?) {
    var chargerId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Driivz API Integration Example")
        Spacer(modifier = Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = { navController?.navigate("login") }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = { navController?.navigate("map") }) {
            Text("Sites map (OTP)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(

        ) {
            TextField(
                modifier = Modifier.weight(0.5f).height(60.dp),
                value = chargerId,
                onValueChange = { chargerId = it },
                label = { Text("Charger ID") }
            )
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.weight(0.5f).height(60.dp),
                onClick = { navController?.navigate("payment/${chargerId}") }) {
                Text("Charge OTP")
            }
        }

    }
}

@Preview
@Composable
fun PreviewCluster() {
    MainScreen(null)
}