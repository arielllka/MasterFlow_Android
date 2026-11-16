package com.ariellka.masterflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace
import java.util.Locale
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    onAppointmentClick: (Appointment) -> Unit,
    records: List<Appointment> = appointments,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {},
    onShare: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    darkTheme: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    scheduleState: LazyListState = rememberLazyListState()
) {
    val locale = Locale.forLanguageTag("ru")
    val date = dashboardDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
        .replaceFirstChar { it.titlecase(locale) }

    val nextAppointment = records.minByOrNull { it.startTime }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxSize().testTag("home-list"),
            state = listState,
            contentPadding = PaddingValues(FlowSpace.page),
            verticalArrangement = Arrangement.spacedBy(FlowSpace.large)
        ) {
            item(key = "header") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.home), style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.semantics { heading() })
                        Text(date, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("toggle-theme")) {
                        Icon(painterResource(if (darkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                            contentDescription = stringResource(if (darkTheme) R.string.light_theme else R.string.dark_theme))
                    }
                    IconButton(onClick = onShare) {
                        Icon(painterResource(R.drawable.ic_share), stringResource(R.string.share_app))
                    }
                }
            }
            item(key = "overview") {
                Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
                    SectionTitle(stringResource(R.string.today_overview))
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
                        SummaryCard(records.size.toString(), stringResource(R.string.appointments), R.drawable.ic_calendar,
                            Modifier.weight(1f).fillMaxHeight(), today = true)
                        SummaryCard("${dashboardCurrency}${records.sumOf { it.price }}", stringResource(R.string.revenue), R.drawable.ic_wallet,
                            Modifier.weight(1f).fillMaxHeight())
                    }
                    Button(
                        onClick = { onNavigate("new-appointment") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = FlowSpace.button),
                        shape = FlowCorners.button
                    ) {
                        Icon(painterResource(R.drawable.ic_plus), null)
                        Text(stringResource(R.string.new_appointment), Modifier.padding(start = FlowSpace.small))
                    }
                }
            }
            if (nextAppointment != null) item(key = "next") {
                Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                    SectionHeader(stringResource(R.string.next_appointment)) { onNavigate("schedule") }
                    Surface(onClick = { onAppointmentClick(nextAppointment) }, shape = FlowCorners.card,
                        color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.fillMaxWidth().padding(FlowSpace.page),
                            verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                            Text(nextAppointment.startTime.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.labelLarge)
                            AppointmentRow(nextAppointment)
                        }
                    }
                }
            }
            item(key = "schedule") {
                Column(Modifier.testTag("schedule-section"), verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                    SectionHeader(stringResource(R.string.today_schedule)) { onNavigate("schedule") }
                    Surface(shape = FlowCorners.card, color = MaterialTheme.colorScheme.surfaceVariant) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(FlowSpace.scheduleViewport).testTag("schedule-list"),
                            state = scheduleState,
                            contentPadding = PaddingValues(horizontal = FlowSpace.page)
                        ) {
                            items(count = records.size, key = { records[it].id }) { index ->
                                val appointment = records[index]
                                AppointmentRow(
                                    appointment = appointment,
                                    modifier = Modifier.clickable(role = Role.Button) { onAppointmentClick(appointment) }
                                        .testTag("open-appointment-${index + 1}")
                                        .padding(vertical = FlowSpace.medium)
                                )
                                if (index < records.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, @DrawableRes icon: Int, modifier: Modifier, today: Boolean = false) {
    Surface(modifier, shape = FlowCorners.card, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(FlowSpace.page), verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), null)
                Spacer(Modifier.weight(1f))
                if (today) {
                    Surface(shape = FlowCorners.pill, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(stringResource(R.string.today), Modifier.padding(horizontal = FlowSpace.small, vertical = FlowSpace.tiny),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Normal)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppointmentRow(appointment: Appointment, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
        Box(Modifier.size(FlowSpace.avatar).background(MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_person), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(Modifier.weight(1f)) {
            Text(appointment.clientName, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.appointment_service, appointment.serviceName, appointment.durationMinutes), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SectionTitle(title, Modifier.weight(1f))
        TextButton(onClick = onViewAll,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)) { Text(stringResource(R.string.view_all)) }
    }
}
