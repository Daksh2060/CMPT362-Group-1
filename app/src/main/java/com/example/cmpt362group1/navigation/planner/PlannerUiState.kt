package com.example.cmpt362group1.navigation.planner

sealed interface PlannerUiState {
    data object Loading : PlannerUiState
    data class Content(
        val sections: List<DaySection>,
        val query: String = ""
    ) : PlannerUiState
    data class Empty(val hint: String = "No Events Found...") : PlannerUiState
    data class Error(val message: String) : PlannerUiState
}
