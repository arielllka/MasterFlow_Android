package com.ariellka.masterflow

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination : NavKey {
    @Serializable data object Home : AppDestination
    @Serializable data object Schedule : AppDestination
    @Serializable data class Detail(val appointmentId: Int) : AppDestination
    @Serializable data object Clients : AppDestination
    @Serializable data object Services : AppDestination
    @Serializable data object NewAppointment : AppDestination
    @Serializable data object InvalidLink : AppDestination
}
