package com.ariellka.masterflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace
import com.ariellka.masterflow.ui.theme.LARGE_FONT_SCALE
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
    onShare: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    darkTheme: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
) {
    val locale = LocalConfiguration.current.locales[0]
    val date = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
        .replaceFirstChar { it.titlecase(locale) }

    val records = uiState.appointments
    val nextAppointment = uiState.nextAppointment
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    val scrollEnabled = largeFont || listState.canScrollForward || listState.canScrollBackward

    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val viewportHeight = (maxHeight - FlowSpace.page * 2).coerceAtLeast(0.dp)
        LazyColumn(
            modifier = Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxSize().testTag("home-list"),
            state = listState,
            userScrollEnabled = scrollEnabled,
            contentPadding = PaddingValues(FlowSpace.page)
        ) {
            item(key = "dashboard") {
                FittingHomeContent(
                    viewportHeight = viewportHeight,
                    appointments = records,
                    onOpen = { onAction(DashboardAction.OpenAppointment(it)) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.large)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(date, style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier.semantics { heading() })

                            }
                            IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("toggle-theme")) {
                                Icon(painterResource(if (darkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                                    contentDescription = stringResource(if (darkTheme) R.string.light_theme else R.string.dark_theme))
                            }
                            IconButton(onClick = onShare) {
                                Icon(painterResource(R.drawable.ic_share), stringResource(R.string.share_app))
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
                            SectionTitle(stringResource(R.string.today_overview))
                            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
                                SummaryCard(uiState.appointmentCount.toString(), stringResource(R.string.appointments), R.drawable.ic_calendar,
                                    Modifier.weight(1f).fillMaxHeight(), today = true)
                                SummaryCard("${uiState.currency}${uiState.totalRevenue}", stringResource(R.string.revenue), R.drawable.ic_wallet,
                                    Modifier.weight(1f).fillMaxHeight())
                            }
                            Button(
                                onClick = { onAction(DashboardAction.OpenScreen("new-appointment")) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = FlowSpace.button),
                                shape = FlowCorners.button
                            ) {
                                Icon(painterResource(R.drawable.ic_plus), null)
                                Text(stringResource(R.string.new_appointment), Modifier.padding(start = FlowSpace.small))
                            }
                        }
                        if (nextAppointment != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                                SectionHeader(stringResource(R.string.next_appointment), "view-all-next") {
                                    onAction(DashboardAction.OpenScreen(AppLinks.SCHEDULE))
                                }
                                Surface(onClick = { onAction(DashboardAction.OpenAppointment(nextAppointment.id)) }, shape = FlowCorners.card,
                                    color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Column(Modifier.fillMaxWidth().padding(FlowSpace.page),
                                        verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                                        Text(nextAppointment.startTime.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.labelLarge)
                                        AppointmentRow(nextAppointment)
                                    }
                                }
                            }
                        }
                        SectionHeader(stringResource(R.string.today_schedule), "view-all-schedule") {
                            onAction(DashboardAction.OpenScreen(AppLinks.SCHEDULE))
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
internal fun AppointmentRow(appointment: Appointment, modifier: Modifier = Modifier) {
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
private fun SectionHeader(title: String, actionTag: String, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SectionTitle(title, Modifier.weight(1f))
        TextButton(onClick = onViewAll, modifier = Modifier.testTag(actionTag),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)) { Text(stringResource(R.string.view_all)) }
    }
}
