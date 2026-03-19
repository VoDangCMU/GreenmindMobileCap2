package com.vodang.greenmind.api.survey

import kotlinx.serialization.Serializable

@Serializable
data class SurveyDto(
    val id: String,
    val title: String,
    val description: String,
    val questionCount: Int,
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false
)

@Serializable
data class SurveyDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<SurveyQuestionDto>
)

@Serializable
data class SurveyQuestionDto(
    val id: String,
    val text: String,
    val type: String, // "single_choice", "multiple_choice", "text"
    val options: List<String> = emptyList()
)

@Serializable
data class SurveySubmitRequest(
    val surveyId: String,
    val answers: List<SurveyAnswerDto>
)

@Serializable
data class SurveyAnswerDto(
    val questionId: String,
    val selectedOptions: List<String> = emptyList(),
    val textValue: String? = null
)

// Mock data — replace with real API calls when backend is ready
val mockSurveys = listOf(
    SurveyDto("1", "Household Waste Habits", "Help us understand how you sort and manage household waste.", 8, 5),
    SurveyDto("2", "Green Meal Awareness", "Tell us about your dietary habits and eco-friendly food choices.", 6, 4, isCompleted = true),
    SurveyDto("3", "Energy Consumption Survey", "Understand your electricity and water usage patterns.", 10, 7),
    SurveyDto("4", "Transport & Carbon Footprint", "How you commute matters for the environment.", 7, 5),
    SurveyDto("5", "Community Volunteering Interest", "Share your interest in environmental volunteer programs.", 5, 3),
)

fun getMockSurveyDetail(id: String): SurveyDetailDto = when (id) {
    "1" -> SurveyDetailDto(
        id = "1",
        title = "Household Waste Habits",
        description = "Help us understand how you sort and manage household waste.",
        questions = listOf(
            SurveyQuestionDto("q1", "How often do you sort your waste?", "single_choice",
                listOf("Every day", "A few times a week", "Rarely", "Never")),
            SurveyQuestionDto("q2", "Which waste types do you separate?", "multiple_choice",
                listOf("Organic", "Plastic", "Paper", "Metal", "Glass")),
            SurveyQuestionDto("q3", "How many bags of waste do you produce per week?", "single_choice",
                listOf("1 bag", "2–3 bags", "4–5 bags", "More than 5")),
            SurveyQuestionDto("q4", "Do you compost organic waste?", "single_choice",
                listOf("Yes, regularly", "Sometimes", "No, but I want to", "No")),
            SurveyQuestionDto("q5", "What prevents you from sorting waste better?", "multiple_choice",
                listOf("Lack of bins", "No time", "Don't know how", "Nothing — I sort well")),
            SurveyQuestionDto("q6", "Do you reuse plastic bags?", "single_choice",
                listOf("Always", "Sometimes", "Rarely", "Never")),
            SurveyQuestionDto("q7", "Are you aware of your building's waste collection schedule?", "single_choice",
                listOf("Yes, I follow it", "Yes, but I forget", "Not really", "No")),
            SurveyQuestionDto("q8", "Any suggestions for improving waste management?", "text"),
        )
    )
    "3" -> SurveyDetailDto(
        id = "3",
        title = "Energy Consumption Survey",
        description = "Understand your electricity and water usage patterns.",
        questions = listOf(
            SurveyQuestionDto("q1", "How many people live in your household?", "single_choice",
                listOf("1", "2–3", "4–5", "More than 5")),
            SurveyQuestionDto("q2", "How much electricity does your household use per month (kWh)?", "single_choice",
                listOf("Less than 100", "100–200", "200–400", "More than 400")),
            SurveyQuestionDto("q3", "Do you use energy-saving light bulbs?", "single_choice",
                listOf("All bulbs", "Most bulbs", "A few", "None")),
            SurveyQuestionDto("q4", "Which appliances use the most electricity in your home?", "multiple_choice",
                listOf("Air conditioner", "Water heater", "Refrigerator", "Washing machine", "TV/Electronics")),
            SurveyQuestionDto("q5", "Do you turn off devices when not in use?", "single_choice",
                listOf("Always", "Usually", "Sometimes", "Rarely")),
            SurveyQuestionDto("q6", "Do you have solar panels installed?", "single_choice",
                listOf("Yes", "No, but planning to", "No")),
            SurveyQuestionDto("q7", "How much water does your household use per month (m³)?", "single_choice",
                listOf("Less than 5", "5–10", "10–20", "More than 20")),
            SurveyQuestionDto("q8", "Do you collect and reuse rainwater?", "single_choice",
                listOf("Yes", "Sometimes", "No")),
            SurveyQuestionDto("q9", "What motivates you to save energy?", "multiple_choice",
                listOf("Save money", "Help the environment", "Reduce carbon footprint", "Social responsibility")),
            SurveyQuestionDto("q10", "Any additional notes about your energy usage?", "text"),
        )
    )
    else -> SurveyDetailDto(
        id = id,
        title = "General Environmental Survey",
        description = "Share your environmental habits and opinions.",
        questions = listOf(
            SurveyQuestionDto("q1", "How would you rate your environmental awareness?", "single_choice",
                listOf("Very high", "High", "Medium", "Low")),
            SurveyQuestionDto("q2", "Which green activities do you practice?", "multiple_choice",
                listOf("Recycling", "Walking/cycling", "Buying local", "Reducing plastic", "Saving energy")),
            SurveyQuestionDto("q3", "What is your biggest environmental concern?", "single_choice",
                listOf("Air pollution", "Water pollution", "Climate change", "Deforestation", "Plastic waste")),
            SurveyQuestionDto("q4", "How often do you participate in environmental events?", "single_choice",
                listOf("Every month", "A few times a year", "Rarely", "Never")),
            SurveyQuestionDto("q5", "Share any thoughts or ideas about the environment:", "text"),
        )
    )
}
