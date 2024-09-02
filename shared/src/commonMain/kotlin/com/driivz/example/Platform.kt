package com.driivz.example

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform