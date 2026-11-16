package com.ariellka.masterflow

import java.net.URI

object AppLinks {
    const val INVALID = "invalid"
    const val NOT_FOUND = "not-found"
    const val LOADING = "loading"
    const val EMPTY = "empty"
    const val ERROR = "error"
    const val HOME_LINK = "masterflow://home"
    const val SCHEDULE_LINK = "masterflow://schedule"
    fun appointmentLink(id: Int) = "masterflow://appointment/$id"

    // Used for both a cold start and onNewIntent.
    fun resolve(link: String?): AppDestination {
        if (link == null) return AppDestination.Home
        if (link == "masterflow:" || link == "masterflow://") return AppDestination.Home
        val uri = runCatching { URI(link) }.getOrNull() ?: return AppDestination.InvalidLink
        if (!uri.scheme.equals("masterflow", ignoreCase = true)) return AppDestination.InvalidLink
        if (uri.isOpaque || uri.userInfo != null || uri.port != -1) return AppDestination.InvalidLink
        val path = uri.path.orEmpty().trimEnd('/')
        return when (uri.host?.lowercase()) {
            "home" -> if (path.isEmpty()) AppDestination.Home else AppDestination.InvalidLink
            "schedule" -> if (path.isEmpty()) AppDestination.Schedule else AppDestination.InvalidLink
            "appointment", "detail" -> {
                val id = path.removePrefix("/").toIntOrNull()
                if (id != null && id > 0) AppDestination.Detail(id) else AppDestination.InvalidLink
            }
            null, "" -> when (path) {
                "", "/home" -> AppDestination.Home
                "/schedule" -> AppDestination.Schedule
                else -> AppDestination.InvalidLink
            }
            else -> AppDestination.InvalidLink
        }
    }
}
