package com.ariellka.masterflow


import androidx.navigation3.runtime.NavKey

internal fun initialBackStack(link: String?): List<AppDestination> = when (val destination = AppLinks.resolve(link)) {
    AppDestination.Home -> listOf(AppDestination.Home)
    is AppDestination.Detail -> listOf(AppDestination.Home, AppDestination.Schedule, destination)
    else -> listOf(AppDestination.Home, destination)
}

internal fun MutableList<NavKey>.navigateTo(destination: AppDestination) {
    if (destination == AppDestination.Home) {
        while (size > 1) removeAt(lastIndex)
        return
    }
    val existing = indexOfLast { it == destination }
    if (existing >= 0) {
        while (lastIndex > existing) removeAt(lastIndex)
    } else {
        add(destination)
    }
}

internal fun MutableList<NavKey>.openLink(link: String?) {
    val destination = AppLinks.resolve(link)
    if (destination is AppDestination.Detail && size == 1 && last() == AppDestination.Home) {
        add(AppDestination.Schedule)
    }
    navigateTo(destination)
}

internal fun MutableList<NavKey>.popDestination(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}
