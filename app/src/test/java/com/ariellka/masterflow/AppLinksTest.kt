package com.ariellka.masterflow

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLinksTest {
    @Test fun scheduleLinksOpenTheSchedule() {
        listOf(AppLinks.SCHEDULE_LINK, "masterflow://schedule/", "masterflow:///schedule")
            .forEach { assertEquals(AppLinks.SCHEDULE, AppLinks.resolve(it)) }
        assertEquals(AppLinks.INVALID, AppLinks.resolve("masterflow://schedule/1"))
    }
    @Test fun launcherAndHomeLinksOpenHome() {
        listOf(null, "masterflow://home", "masterflow://home/", "masterflow://", "masterflow:///home")
            .forEach { assertEquals(it, AppLinks.HOME, AppLinks.resolve(it)) }
    }

    @Test fun appointmentLinksIncludeOriginalAndAdditionalIds() {
        listOf(1, 2, 42, 4, 8).forEach {
            assertEquals(AppLinks.taskDestination(it), AppLinks.resolve(AppLinks.taskLink(it)))
        }
    }

    @Test fun invalidLinksNeverOpenHomeOrAnUnrelatedTask() {
        listOf("masterflow://task/999", "masterflow://task/3", "masterflow://task/nope",
            "masterflow://task", "masterflow://home/1", "https://task/1", "masterflow://unknown",
            "masterflow:garbage", "masterflow:task/1", "masterflow://task/1/2", "masterflow://user@home", "masterflow://home:80", "not a uri")
            .forEach { assertEquals(it, AppLinks.INVALID, AppLinks.resolve(it)) }
    }

    @Test fun sharingAlwaysProducesAHomeLink() {
        assertEquals(AppLinks.HOME, AppLinks.resolve(AppLinks.HOME_LINK))
    }
}
