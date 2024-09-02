package com.driivz.example.manager

import ServiceAccountImpl
import com.driivz.example.security.ServiceAccount
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceAccountManager(config: ApplicationConfig) {
    private val mutex = Mutex()

    private val baseURL = config.property("ktor.serviceAccount.baseURL").getString()
    private val userName = config.property("ktor.serviceAccount.userName").getString()
    private val password = config.property("ktor.serviceAccount.password").getString()

    private var serviceAccount: ServiceAccount? = null

    suspend fun getServiceAccount(): ServiceAccount? {
        return mutex.withLock {
            if (serviceAccount == null || !serviceAccount!!.isTokenValid()) {
                serviceAccount = ServiceAccountImpl(userName, password, baseURL)
                if (serviceAccount?.login() == false) {
                    throw Exception("Failed to log in to the service account")
                }
            }
            serviceAccount
        }
    }
}