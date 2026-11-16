package com.ariellka.masterflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace

@Composable
internal fun FittingHomeContent(
    viewportHeight: Dp,
    appointments: List<Appointment>,
    onOpen: (Int) -> Unit,
    sections: @Composable () -> Unit
) {
    SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
        val naturalConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val sectionsPlaceable = subcompose("sections", sections).single().measure(naturalConstraints)
        val gap = FlowSpace.small.roundToPx()
        val remainingHeight = (viewportHeight.roundToPx() - sectionsPlaceable.height - gap).coerceAtLeast(0)
        val schedulePlaceable = subcompose("schedule") {
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("schedule-list"),
                shape = FlowCorners.card,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Layout(
                    modifier = Modifier.padding(horizontal = FlowSpace.page),
                    content = {
                        appointments.forEachIndexed { index, appointment ->
                            Column {
                                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                AppointmentRow(
                                    appointment,
                                    Modifier.clickable(role = Role.Button) { onOpen(appointment.id) }
                                        .testTag("open-appointment-${index + 1}")
                                        .padding(vertical = FlowSpace.medium)
                                )
                            }
                        }
                    }
                ) { measurables, rowConstraints ->
                    val rows = measurables.map { it.measure(rowConstraints.copy(minHeight = 0)) }
                    var visibleCount = 0
                    var height = 0
                    for (row in rows) {
                        if (visibleCount >= 2 && height + row.height > remainingHeight) break
                        height += row.height
                        visibleCount++
                    }
                    layout(rowConstraints.maxWidth, height) {
                        var y = 0
                        rows.take(visibleCount).forEach { row ->
                            row.placeRelative(0, y)
                            y += row.height
                        }
                    }
                }
            }
        }.single().measure(naturalConstraints)
        layout(constraints.maxWidth, sectionsPlaceable.height + gap + schedulePlaceable.height) {
            sectionsPlaceable.placeRelative(0, 0)
            schedulePlaceable.placeRelative(0, sectionsPlaceable.height + gap)
        }
    }
}
