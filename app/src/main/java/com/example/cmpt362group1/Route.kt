package com.example.cmpt362group1

sealed class Route(val route: String) {
    object Login : Route("login")
    object Explore : Route("explore")
    object Planner : Route("planner")
    object Profile : Route("profile")
    object CreateEvent : Route("create_event")
}
