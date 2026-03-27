package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MealRecord(
    val id: String,
    val plantRatio: Int,
    val description: String,
    val timestampMillis: Long,
)

object MealStore {
    private val _meals = MutableStateFlow<List<MealRecord>>(emptyList())
    val meals: StateFlow<List<MealRecord>> = _meals.asStateFlow()

    fun add(plantRatio: Int, description: String) {
        val record = MealRecord(
            id = kotlin.random.Random.nextLong().toString(),
            plantRatio = plantRatio,
            description = description,
            timestampMillis = System.currentTimeMillis(),
        )
        _meals.value = listOf(record) + _meals.value
    }
}
