package com.vodang.greenmind

/** Format a [Double] to [decimals] decimal places. */
fun Double.fmt(decimals: Int): String = String.format("%.${decimals}f", this)

/** Format a [Float] to [decimals] decimal places. */
fun Float.fmt(decimals: Int): String = String.format("%.${decimals}f", this)

/** Multiplatform [String.format] replacement. */
fun String.fmt(vararg args: Any?): String = String.format(this, *args)
