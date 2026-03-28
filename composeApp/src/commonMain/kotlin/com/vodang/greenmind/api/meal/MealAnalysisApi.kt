package com.vodang.greenmind.api.meal

import kotlinx.coroutines.delay

data class MealAnalysisResult(val plantRatio: Int, val description: String)

// TODO: Replace with real API call to the meal-analysis endpoint.
//       Expected request: POST /scan/meal  { image: base64 | multipart }
//       Expected response: MealAnalysisResult (plantRatio: Int 0-100, description: String)
//       Remove mockMeals and the random() call in analyzeMeal() once the endpoint is ready.
private val mockMeals = listOf(
    MealAnalysisResult(82, "Salad with mixed greens, tomatoes, and cucumber"),
    MealAnalysisResult(74, "Stir-fried vegetables with tofu and rice"),
    MealAnalysisResult(65, "Vegetable soup with bread"),
    MealAnalysisResult(55, "Rice bowl with grilled chicken and steamed broccoli"),
    MealAnalysisResult(48, "Pasta with marinara sauce and side salad"),
    MealAnalysisResult(38, "Grilled salmon with roasted potatoes and green beans"),
    MealAnalysisResult(30, "Beef stir-fry with a small portion of vegetables"),
    MealAnalysisResult(71, "Buddha bowl with quinoa, avocado, and roasted vegetables"),
)

suspend fun analyzeMeal(imageBytes: ByteArray): MealAnalysisResult {
    // TODO: Replace mock with real HTTP call. Remove delay() too.
    delay(1_500) // simulate network latency
    return mockMeals.random()
}
