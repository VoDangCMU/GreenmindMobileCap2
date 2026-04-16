package com.vodang.greenmind.householdwaste

import com.vodang.greenmind.wastesort.WasteSortStatus

internal fun parseWasteSortStatus(status: String?): WasteSortStatus {
    return when(status) {
        "brought_out" -> WasteSortStatus.BRINGOUTED
        "picked_up", "done", "completed" -> WasteSortStatus.COLLECTED
        else -> WasteSortStatus.SORTED
    }
}
