package com.driivz.example.network

import android.content.SharedPreferences

class TokenProvider(private val sharedPreferences: SharedPreferences) {
    fun isAuthenticated(): Boolean {
        return getToken().isNotEmpty()
    }

    fun getToken(): String {
        return sharedPreferences.getString("jwt_token", "") ?: ""
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("jwt_token", token).apply()
    }
}