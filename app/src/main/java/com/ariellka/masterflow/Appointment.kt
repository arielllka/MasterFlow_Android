package com.ariellka.masterflow

import java.time.LocalTime

data class Appointment(
    val id: Int,
    val clientName: String,
    val serviceName: String,
    val startTime: LocalTime,
    val durationMinutes: Int,
    val price: Int
)
