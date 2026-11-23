package com.ariellka.masterflow

import java.time.LocalDate

enum class DashboardStatus { LOADING, CONTENT, EMPTY, EMPTY_SEARCH, ERROR }


data class DashboardUiState(
    val status: DashboardStatus = DashboardStatus.LOADING,
    val date: LocalDate = dashboardDate,
    val currency: String = dashboardCurrency,
    val appointments: List<Appointment> = emptyList(),
    val appointmentsById: Map<Int, Appointment> = emptyMap(),
    val scheduleAppointments: List<Appointment> = emptyList(),
    val services: List<Service> = emptyList(),
    val selectedServiceId: Int = 0,
    val appointmentCount: Int = 0,
    val totalRevenue: Int = 0,
    val nextAppointment: Appointment? = null,
    val searchQuery: String = "",
    val timedOut: Boolean = false,
    val pendingNavigation: DashboardNavigation? = null
)

data class DashboardNavigation(val id: Long, val destination: AppDestination)

sealed interface DashboardAction {
    data object Refresh : DashboardAction
    data object Retry : DashboardAction
    data class QueryChanged(val query: String) : DashboardAction
    data object ClearQuery : DashboardAction
    data class ServiceChanged(val serviceId: Int) : DashboardAction
    data object ClearFilters : DashboardAction
    data class OpenScreen(val destination: AppDestination) : DashboardAction
    data class OpenAppointment(val id: Int) : DashboardAction
    data class NavigationHandled(val id: Long) : DashboardAction
}
