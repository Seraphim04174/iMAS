package com.imas.imas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform