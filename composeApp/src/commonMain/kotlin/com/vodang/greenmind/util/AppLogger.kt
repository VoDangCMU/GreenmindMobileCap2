package com.vodang.greenmind.util

/**
 * Common logger with a fixed tag for easy logcat filtering.
 *
 * Usage:  `adb logcat | grep GreenMind`
 */
object AppLogger {
    const val TAG = "GreenMind"

    fun d(subtag: String, message: String) {
        println("[$TAG][$subtag] $message")
    }

    fun i(subtag: String, message: String) {
        println("[$TAG][$subtag] $message")
    }

    fun w(subtag: String, message: String) {
        println("[$TAG][$subtag] ⚠ $message")
    }

    fun e(subtag: String, message: String, throwable: Throwable? = null) {
        println("[$TAG][$subtag] ✖ $message")
        throwable?.let { println("[$TAG][$subtag] ✖ ${it.stackTraceToString()}") }
    }
}
