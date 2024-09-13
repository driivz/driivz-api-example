package com.driivz.example.stripe.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.driivz.example.viewmodel.LoginState
import com.driivz.example.viewmodel.LoginViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = getViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by loginViewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginViewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginState.Loading
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
            } else {
                Text("Login")
            }
        }

        Button(onClick = {
            password = "***REMOVED***"
            username = "***REMOVED***"
        }) {
            Text("Fill")
        }

        when (loginState) {
            is LoginState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
            is LoginState.Success -> {
                LaunchedEffect(Unit) {
                    // Navigate to the next screen after successful login
                    navController.navigate("paymentMethods")
                }
            }
            else -> {}
        }
    }
}