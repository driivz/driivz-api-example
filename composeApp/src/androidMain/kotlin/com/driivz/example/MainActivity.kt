package com.driivz.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.driivz.example.screen.ChargerListScreen
import com.driivz.example.screen.PaymentListScreen
import com.driivz.example.stripe.StripeService
import com.driivz.example.stripe.screen.LoginScreen
import com.driivz.example.stripe.screen.MainScreen
import com.driivz.example.stripe.screen.MapScreen
import com.driivz.example.stripe.screen.PaymentScreen
import com.driivz.example.viewmodel.ChargerListViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.getViewModel

class MainActivity : AppCompatActivity() {
    private val stripe: StripeService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExampleApp()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stripe.onSetupResult(requestCode, data)
    }
}

@Composable
fun ExampleApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("map") { MapScreen(navController) }
        composable("payment") { PaymentScreen(navController) }
        composable("payment/{CHARGER_ID}", arguments = listOf(
            navArgument("CHARGER_ID") { type = NavType.LongType }
        )) {
            val chargerId = it.arguments?.getLong("CHARGER_ID")
            PaymentScreen(navController, true, chargerId)
        }
        composable("paymentMethods") { PaymentListScreen(navController) }
        composable("chargersList/{SITE_ID}", arguments = listOf(
            navArgument("SITE_ID") { type = NavType.LongType }
        )) {
            val siteId = it.arguments?.getLong("SITE_ID") ?: 0L
            ChargerListScreen(siteId, navController)
        }
    }
}