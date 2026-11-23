package com.ariellka.masterflow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    loadRecords: suspend () -> List<Appointment> = { appointments },
    loadingDelayMillis: Long = LOADING_DELAY_MILLIS,
    private val timeoutMillis: Long = LOAD_TIMEOUT_MILLIS,
    loadServices: suspend () -> List<Service> = { services }
) : ViewModel() {
    private val repository = AppointmentRepository(loadRecords, loadServices, loadingDelayMillis)
    val query: StateFlow<String> = savedStateHandle.getStateFlow(SEARCH_QUERY_KEY, "")
    private val selectedService = savedStateHandle.getStateFlow(SERVICE_FILTER_KEY, 0)
    private val refreshVersion = MutableStateFlow(0)
    private val navigation = MutableStateFlow<DashboardNavigation?>(null)
    private var initialized = false
    private var navigationId = 0L

    val uiState: StateFlow<DashboardUiState> = combine(
        searchResults(), query, selectedService, navigation
    ) { result, text, serviceId, event ->
        result.copy(searchQuery = text, selectedServiceId = serviceId, pendingNavigation = event)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = DashboardUiState(searchQuery = query.value, selectedServiceId = selectedService.value)
    )

    fun loadOnce() {
        if (!initialized) refresh()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.Refresh, DashboardAction.Retry -> refresh()
            is DashboardAction.QueryChanged -> updateQuery(action.query)
            DashboardAction.ClearQuery -> updateQuery("")
            is DashboardAction.ServiceChanged -> savedStateHandle[SERVICE_FILTER_KEY] = action.serviceId
            DashboardAction.ClearFilters -> {
                updateQuery("")
                savedStateHandle[SERVICE_FILTER_KEY] = 0
            }
            is DashboardAction.OpenScreen -> publishNavigation(action.destination)
            is DashboardAction.OpenAppointment -> {
                if (uiState.value.appointmentsById.containsKey(action.id)) {
                    publishNavigation(AppDestination.Detail(action.id))
                }
            }
            is DashboardAction.NavigationHandled -> navigation.update {
                if (it?.id == action.id) null else it
            }
        }
    }

    fun refresh() {
        initialized = true
        refreshVersion.update { it + 1 }
    }

    private fun searchResults(): Flow<DashboardUiState> {
        val searchQuery = query
            .map { it.trim().lowercase(Locale.ROOT) }
            .debounce(SEARCH_DEBOUNCE_MILLIS.milliseconds)
            .distinctUntilChanged()
        return combine(searchQuery, selectedService, refreshVersion) { text, serviceId, _ ->
            text to serviceId
        }.flatMapLatest { (text, serviceId) ->
            search(text, serviceId)
        }.onStart { emit(loadingState()) }
    }

    private fun search(text: String, serviceId: Int): Flow<DashboardUiState> = flow {
        val result = withTimeout(timeoutMillis.milliseconds) {
            repository.search(text, serviceId)
        }
        val records = result.appointments
        emit(DashboardUiState(
            status = when {
                records.isEmpty() -> DashboardStatus.EMPTY
                result.matchingAppointments.isEmpty() -> DashboardStatus.EMPTY_SEARCH
                else -> DashboardStatus.CONTENT
            },
            appointments = records,
            appointmentsById = records.associateBy { it.id },
            scheduleAppointments = result.matchingAppointments,
            services = result.services,
            appointmentCount = records.size,
            totalRevenue = records.sumOf { it.price },
            nextAppointment = records.firstOrNull()
        ))
    }.onStart {
        initialized = true
        emit(loadingState())
    }.catch { error ->
        if (error is CancellationException && error !is TimeoutCancellationException) throw error
        emit(uiState.value.copy(status = DashboardStatus.ERROR, timedOut = error is TimeoutCancellationException))
    }

    private fun loadingState() = uiState.value.copy(status = DashboardStatus.LOADING, timedOut = false)

    private fun updateQuery(text: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = text
    }

    private fun publishNavigation(destination: AppDestination) {
        val event = DashboardNavigation(++navigationId, destination)
        navigation.value = event
    }

    companion object {
        const val SEARCH_QUERY_KEY = "schedule_search_query"
        const val SERVICE_FILTER_KEY = "schedule_service_filter"
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val LOADING_DELAY_MILLIS = 2_000L
        const val LOAD_TIMEOUT_MILLIS = 10_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(savedStateHandle = createSavedStateHandle()) }
        }
    }
}
