package com.ariellka.masterflow

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Content(val records: List<Appointment>) : DashboardState
    data object Empty : DashboardState
    data class Error(val timedOut: Boolean = false) : DashboardState
}

class DashboardViewModel(
    private val loadRecords: suspend () -> List<Appointment> = { appointments },
    private val loadingDelayMillis: Long = 2_000,
    private val timeoutMillis: Long = 10_000
) : ViewModel() {
    var state by mutableStateOf<DashboardState>(DashboardState.Loading)
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var loadingJob: Job? = null
    private var initialized = false

    fun loadOnce() {
        if (!initialized) {
            initialized = true
            refresh()
        }
    }

    fun refresh() {
        loadingJob?.cancel()
        loadingJob = scope.launch {
            state = DashboardState.Loading
            state = try {
                val records = withTimeout(timeoutMillis.milliseconds) {
                    delay(loadingDelayMillis.milliseconds)
                    loadRecords()
                }
                if (records.isEmpty()) DashboardState.Empty else DashboardState.Content(records)
            } catch (timeout: TimeoutCancellationException) {
                DashboardState.Error(timedOut = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("MasterFlow", "Не удалось загрузить расписание", error)
                DashboardState.Error()
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}
