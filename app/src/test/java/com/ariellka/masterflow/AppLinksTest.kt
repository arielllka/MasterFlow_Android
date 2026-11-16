package com.ariellka.masterflow

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLinksTest {
    @Test fun scheduleLinksOpenTheSchedule() {
        listOf(AppLinks.SCHEDULE_LINK, "masterflow://schedule/", "masterflow:///schedule")
            .forEach { assertEquals(AppDestination.Schedule, AppLinks.resolve(it)) }
        assertEquals(AppDestination.InvalidLink, AppLinks.resolve("masterflow://schedule/1"))
    }
    @Test fun launcherAndHomeLinksOpenHome() {
        listOf(null, "masterflow://home", "masterflow://home/", "masterflow://", "masterflow:///home")
            .forEach { assertEquals(it, AppDestination.Home, AppLinks.resolve(it)) }
    }

    @Test fun appointmentLinksIncludeOriginalAndAdditionalIds() {
        listOf(1, 2, 42, 4, 8).forEach {
            assertEquals(AppDestination.Detail(it), AppLinks.resolve(AppLinks.appointmentLink(it)))
        }
    }

    @Test fun invalidLinksNeverOpenHomeOrAnUnrelatedAppointment() {
        listOf("masterflow://appointment/0", "masterflow://appointment/-1", "masterflow://appointment/nope",
            "masterflow://appointment", "masterflow://home/1", "https://appointment/1", "masterflow://unknown",
            "masterflow:garbage", "masterflow:appointment/1", "masterflow://appointment/1/2", "masterflow://user@home", "masterflow://home:80", "not a uri")
            .forEach { assertEquals(it, AppDestination.InvalidLink, AppLinks.resolve(it)) }
    }

    @Test fun sharingAlwaysProducesAHomeLink() {
        assertEquals(AppDestination.Home, AppLinks.resolve(AppLinks.HOME_LINK))
    }
}
