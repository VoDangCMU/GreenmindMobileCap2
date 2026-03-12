package com.vodang.greenmind

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform