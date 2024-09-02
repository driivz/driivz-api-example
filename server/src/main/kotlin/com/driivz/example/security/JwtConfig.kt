package com.driivz.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "my-super-secret-key"
    private const val issuer = "ktor.io"
    private const val audience = "ktor-audience"
    private const val validityInMs = 36_000_00 * 10 // 10 hours

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(ticket: String, accountNumber: Int): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("ticket", ticket)
        .withClaim("accountNumber", accountNumber)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}