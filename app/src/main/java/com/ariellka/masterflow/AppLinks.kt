package com.ariellka.masterflow

import java.net.URI

object AppLinks {
    const val HOME = "home"
    const val INVALID = "invalid"
    const val LOADING = "loading"
    const val EMPTY = "empty"
    const val ERROR = "error"
    const val HOME_LINK = "masterflow://home"
    private val taskIds = appointments.map { it.id }.toSet()

    fun taskDestination(id: Int) = "task/$id"
    fun taskLink(id: Int) = "masterflow://task/$id"

    // Used for both a cold start and onNewIntent.
    fun resolve(link: String?): String {
        if (link == null) return HOME
        if (link == "masterflow:" || link == "masterflow://") return HOME
        val uri = runCatching { URI(link) }.getOrNull() ?: return INVALID
        if (!uri.scheme.equals("masterflow", ignoreCase = true)) return INVALID
        if (uri.isOpaque || uri.userInfo != null || uri.port != -1) return INVALID
        val path = uri.path.orEmpty().trimEnd('/')
        return when (uri.host?.lowercase()) {
            "home" -> if (path.isEmpty()) HOME else INVALID
            "task" -> {
                val id = path.removePrefix("/").toIntOrNull()
                if (id in taskIds) taskDestination(id!!) else INVALID
            }
            null, "" -> if (path.isEmpty() || path == "/home") HOME else INVALID
            else -> INVALID
        }
    }
}
