package com.example.cmpt362group1.navigation.planner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlannerHost(vm: PlannerViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    PlannerScreen(
        uiState = state,
        onSearchChange = vm::onSearchChange,
        onEventClick  = vm::onEventClick,
        onEditClick   = vm::onEditClick,
        onCreateClick = vm::onCreateClick
    )
}
