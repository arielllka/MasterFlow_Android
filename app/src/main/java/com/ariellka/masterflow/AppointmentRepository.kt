package com.ariellka.masterflow

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.time.Duration.Companion.milliseconds

data class AppointmentSearchResult(
    val appointments: List<Appointment>,
    val matchingAppointments: List<Appointment>,
    val services: List<Service>
)

class AppointmentRepository(
    private val loadRecords: suspend () -> List<Appointment> = { appointments },
    private val loadServices: suspend () -> List<Service> = { services },
    private val loadingDelayMillis: Long = DashboardViewModel.LOADING_DELAY_MILLIS
) {
    suspend fun search(query: String, serviceId: Int): AppointmentSearchResult = coroutineScope {
        // Для списка и фильтра нужны оба источника; ошибка одного отменяет всю загрузку.
        val records = async {
            delay(loadingDelayMillis.milliseconds)
            loadRecords()
        }
        val serviceCatalog = async {
            delay(loadingDelayMillis.milliseconds)
            loadServices()
        }
        val allRecords = records.await().sortedWith(compareBy({ it.startTime }, { it.id }))
        val availableServices = serviceCatalog.await()
        val serviceName = availableServices.find { it.id == serviceId }?.name
        val context = currentCoroutineContext()
        val matching = allRecords.filter {
            context.ensureActive()
            it.clientName.contains(query, ignoreCase = true) &&
                (serviceId == 0 || it.serviceName == serviceName)
        }
        AppointmentSearchResult(allRecords, matching, availableServices)
    }
}
