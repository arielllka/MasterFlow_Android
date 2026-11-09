package com.ariellka.masterflow

import java.time.LocalDate
import java.time.LocalTime


private val previewAnna = Appointment(1, "Анна К.", "Услуга", LocalTime.of(14, 30), 60, 3)
private val previewMaria = Appointment(2, "Мария С.", "Услуга", LocalTime.of(15, 30), 60, 3)
private val previewOlga = Appointment(42, "Ольга М.", "Услуга", LocalTime.of(16, 30), 60, 3)

internal val previewDashboardState = DashboardUiState(
    status = DashboardStatus.CONTENT,
    date = LocalDate.of(2026, 11, 9),
    appointments = listOf(previewAnna, previewMaria, previewOlga),
    scheduleAppointments = listOf(previewAnna, previewMaria, previewOlga),
    appointmentCount = 3,
    totalRevenue = 9,
    nextAppointment = previewAnna
)
