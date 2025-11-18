package com.example.cmpt362group1.navigation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class PlannerViewModel(
    private val repo: PlannerRepository = FirestorePlannerRepository()
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<PlannerUiState> =
        query
            .debounce(150)
            .flatMapLatest { q -> repo.streamEvents(q) }
            .map { events ->
                val sections = EventGrouping.groupAndSort(events)
                if (sections.isEmpty()) PlannerUiState.Empty()
                else PlannerUiState.Content(sections, query.value)
            }
            .onStart { emit(PlannerUiState.Loading) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PlannerUiState.Loading
            )

    fun onSearchChange(q: String) { query.value = q }
}
