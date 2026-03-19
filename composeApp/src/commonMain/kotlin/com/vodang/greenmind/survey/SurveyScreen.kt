package com.vodang.greenmind.survey

import androidx.compose.runtime.*
import com.vodang.greenmind.api.survey.SurveyDto
import com.vodang.greenmind.api.survey.getMockSurveyDetail
import com.vodang.greenmind.api.survey.mockSurveys

@Composable
fun SurveyScreen() {
    var selectedSurvey by remember { mutableStateOf<SurveyDto?>(null) }
    var completedIds by remember {
        mutableStateOf(mockSurveys.filter { it.isCompleted }.map { it.id }.toSet())
    }

    val surveys = mockSurveys.map { it.copy(isCompleted = it.id in completedIds) }

    if (selectedSurvey != null) {
        SurveyTakingScreen(
            survey = getMockSurveyDetail(selectedSurvey!!.id),
            onBack = { selectedSurvey = null },
            onSubmit = {
                completedIds = completedIds + selectedSurvey!!.id
                selectedSurvey = null
            }
        )
    } else {
        SurveyListScreen(
            surveys = surveys,
            onSelectSurvey = { selectedSurvey = it }
        )
    }
}
