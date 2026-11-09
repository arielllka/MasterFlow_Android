package com.ariellka.masterflow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class DashboardViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val loadRecords: suspend () -> List<Appointment> = { appointments },
    private val loadingDelayMillis: Long = LOADING_DELAY_MILLIS,
    private val timeoutMillis: Long = LOAD_TIMEOUT_MILLIS
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DashboardUiState(searchQuery = savedStateHandle[SEARCH_QUERY_KEY] ?: "")
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var loadingJob: Job? = null
    private var initialized = false
    private var navigationId = 0L

    fun loadOnce() {
        if (!initialized) refresh()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.Refresh, DashboardAction.Retry -> refresh()
            is DashboardAction.QueryChanged -> updateQuery(action.query)
            DashboardAction.ClearQuery -> updateQuery("")
            is DashboardAction.OpenScreen -> publishNavigation(action.destination)
            is DashboardAction.OpenAppointment -> {
                if (_uiState.value.appointments.any { it.id == action.id }) {
                    publishNavigation(AppLinks.taskDestination(action.id))
                }
            }
            is DashboardAction.NavigationHandled -> _uiState.update {
                if (it.pendingNavigation?.id == action.id) it.copy(pendingNavigation = null) else it
            }
        }
    }

    fun refresh() {
        initialized = true
        loadingJob?.cancel()
        _uiState.update { it.copy(status = DashboardStatus.LOADING, timedOut = false) }
        loadingJob = viewModelScope.launch {
            try {
                val records = withTimeout(timeoutMillis.milliseconds) {
                    delay(loadingDelayMillis.milliseconds)
                    loadRecords()
                }.sortedWith(compareBy({ it.startTime }, { it.id }))
                _uiState.update { old ->
                    old.copy(
                        status = if (records.isEmpty()) DashboardStatus.EMPTY else DashboardStatus.CONTENT,
                        appointments = records,
                        scheduleAppointments = filterRecords(records, old.searchQuery),
                        appointmentCount = records.size,
                        totalRevenue = records.sumOf { it.price },
                        nextAppointment = records.firstOrNull(),
                        timedOut = false
                    )
                }
            } catch (timeout: TimeoutCancellationException) {
                _uiState.update { it.copy(status = DashboardStatus.ERROR, timedOut = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(status = DashboardStatus.ERROR, timedOut = false) }
            }
        }
    }

    private fun updateQuery(query: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = query
        _uiState.update {
            it.copy(searchQuery = query, scheduleAppointments = filterRecords(it.appointments, query))
        }
    }

    private fun publishNavigation(destination: String) {
        val event = DashboardNavigation(++navigationId, destination)
        _uiState.update { it.copy(pendingNavigation = event) }
    }

    private fun filterRecords(records: List<Appointment>, query: String): List<Appointment> {
        val name = query.trim()
        return if (name.isEmpty()) records else records.filter { it.clientName.contains(name, ignoreCase = true) }
    }

    companion object {
        const val SEARCH_QUERY_KEY = "schedule_search_query"
        const val LOADING_DELAY_MILLIS = 2_000L
        const val LOAD_TIMEOUT_MILLIS = 10_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(savedStateHandle = createSavedStateHandle()) }
        }
    }
}
