package com.driivz.example.security

import com.driivz.example.api.AddPaymentCardRequest
import com.driivz.example.api.Charger
import com.driivz.example.api.ChargerFindRequest
import com.driivz.example.api.ChargerProfile
import com.driivz.example.api.OneTimePaymentStartTransaction
import com.driivz.example.api.PaymentCard
import com.driivz.example.api.Site
import com.driivz.example.api.SiteSearchRequest

interface ServiceAccount {
    val ticket: String
    val expirationTime: Long

    fun isTokenValid(): Boolean {
        return System.currentTimeMillis() < expirationTime
    }

    suspend fun login(): Boolean

    suspend fun findCustomerAccountNumber(userName: String): Int?

    suspend fun loginAsCustomer(accountNumber: Int): String?

    suspend fun paymentMethods(accountNumber: Int): List<PaymentCard>?

    suspend fun addPayment(request: AddPaymentCardRequest): PaymentCard?

    suspend fun findSite(siteId: Long?): Site?

    suspend fun findChargerProfile(chargerId: Long?): ChargerProfile?

    suspend fun searchSites(request: SiteSearchRequest): List<Site>?

    suspend fun findChargerLocations(request: ChargerFindRequest): List<Charger>?

    suspend fun oneTimePaymentStartTransaction(
        connectorId:Long,
        request: AddPaymentCardRequest
    ): OneTimePaymentStartTransaction?
}