package com.vodang.greenmind.time

import java.time.*
import java.time.format.DateTimeFormatter

private val LOCAL_ZONE = ZoneId.systemDefault()

/**
 * Convert UTC ISO timestamp to device's local timezone.
 * Input:  "2026-05-03T10:30:00.000Z"
 * Output: "03/05/2026 17:30" (if device is in +07:00 timezone)
 */
fun formatDateTimeLocal(iso: String): String = try {
    val instant = Instant.parse(iso)
    val localDateTime = instant.atZone(LOCAL_ZONE).toLocalDateTime()
    localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (_: Throwable) { iso }

/**
 * Format date only (dd/MM/yyyy) - no timezone conversion needed for dates.
 */
fun formatDateLocal(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

/**
 * Format for chat bubble display: "03/05 17:30"
 */
fun formatChatTime(iso: String): String = try {
    val instant = Instant.parse(iso)
    val localDateTime = instant.atZone(LOCAL_ZONE).toLocalDateTime()
    localDateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
} catch (_: Throwable) { iso }
