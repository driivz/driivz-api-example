package com.driivz.example.di

import android.content.Context
import com.driivz.example.network.TokenProvider
import com.driivz.example.stripe.StripeService
import com.driivz.example.stripe.network.ApiService
import com.driivz.example.viewmodel.LoginViewModel
import com.driivz.example.viewmodel.PaymentListViewModel
import com.driivz.example.viewmodel.PaymentViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CoroutineScope(Dispatchers.Default) }
    single {
        androidContext().getSharedPreferences("app", Context.MODE_PRIVATE)
    }
    single { TokenProvider(get()) }
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    single { ApiService("http://10.0.2.2:8080", get(), get()) }
    single { StripeService(get(), get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { PaymentListViewModel(get()) }
    viewModel { PaymentViewModel(get(), get()) }
}