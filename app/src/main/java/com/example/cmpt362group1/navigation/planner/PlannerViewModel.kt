package com.example.cmpt362group1.navigation.planner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class PlannerViewModel(
    private val repo: PlannerRepository = FirestorePlannerRepository()
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<PlannerUiState> =
        query
            .debounce(150)
            .flatMapLatest { q -> repo.streamEvents(q) }
            .map { events ->
                val today = LocalDate.now()

                val sections = EventGrouping
                    .groupAndSort(events)
                    .filter { it.date >= today }

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
