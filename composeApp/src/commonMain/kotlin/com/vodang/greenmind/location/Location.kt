package com.vodang.greenmind.location

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)
