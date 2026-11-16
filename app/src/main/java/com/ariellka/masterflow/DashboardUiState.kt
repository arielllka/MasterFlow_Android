package com.ariellka.masterflow

import java.time.LocalDate

enum class DashboardStatus { LOADING, CONTENT, EMPTY, ERROR }


data class DashboardUiState(
    val status: DashboardStatus = DashboardStatus.LOADING,
    val date: LocalDate = dashboardDate,
    val currency: String = dashboardCurrency,
    val appointments: List<Appointment> = emptyList(),
    val scheduleAppointments: List<Appointment> = emptyList(),
    val appointmentCount: Int = 0,
    val totalRevenue: Int = 0,
    val nextAppointment: Appointment? = null,
    val searchQuery: String = "",
    val timedOut: Boolean = false,
    val pendingNavigation: DashboardNavigation? = null
)

data class DashboardNavigation(val id: Long, val destination: String)

sealed interface DashboardAction {
    data object Refresh : DashboardAction
    data object Retry : DashboardAction
    data class QueryChanged(val query: String) : DashboardAction
    data object ClearQuery : DashboardAction
    data class OpenScreen(val destination: String) : DashboardAction
    data class OpenAppointment(val id: Int) : DashboardAction
    data class NavigationHandled(val id: Long) : DashboardAction
}
