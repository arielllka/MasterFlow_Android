package com.ariellka.masterflow

import java.time.LocalDate
import java.time.LocalTime


val dashboardDate: LocalDate = LocalDate.now()
const val dashboardCurrency: String = "\$"

val appointments = listOf(
    Appointment(
        id = 1,
        clientName = "Валерия С.",
        serviceName = "Услуга",
        startTime = LocalTime.of(14, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 2,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(15, 30),
        durationMinutes = 60,
        price = 3
    ),

    Appointment(
        id = 42,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(16, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 4,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(17, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 5,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(18, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 6,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(19, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 7,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(20, 30),
        durationMinutes = 60,
        price = 3
    ),
    Appointment(
        id = 8,
        clientName = "Анна К.",
        serviceName = "Услуга",
        startTime = LocalTime.of(21, 30),
        durationMinutes = 60,
        price = 3
    )
)
