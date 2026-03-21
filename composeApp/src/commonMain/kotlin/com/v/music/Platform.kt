package com.v.music

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform