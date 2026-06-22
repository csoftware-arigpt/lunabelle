package com.cycletracker.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cycletracker.app.R
import com.cycletracker.app.core.designsystem.theme.FertileColor
import com.cycletracker.app.core.designsystem.theme.MenstrualColor
import com.cycletracker.app.core.designsystem.theme.OvulationColor
import com.cycletracker.app.core.designsystem.theme.PredictedColor
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onDayClick: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showRangePicker by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.prev_month))
            }
            Text(
                monthLabel(state.year, state.month),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
            }
        }

        WeekdayHeader()
        MonthGrid(state, onDayClick)

        Legend()

        FilledTonalButton(onClick = { showRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_add_period))
        }
    }

    if (showRangePicker) {
        val pickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val s = pickerState.selectedStartDateMillis
                    val e = pickerState.selectedEndDateMillis ?: s
                    if (s != null && e != null) {
                        viewModel.addPeriod(s.toUtcLocalDate(), e.toUtcLocalDate())
                    }
                    showRangePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DateRangePicker(
                state = pickerState,
                modifier = Modifier.height(460.dp),
                title = {
                    Text(
                        stringResource(R.string.action_add_period),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                headline = {
                    Text(
                        stringResource(R.string.select_dates_hint),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                showModeToggle = false,
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val symbols = remember { weekdayInitials() }
    Row(Modifier.fillMaxWidth()) {
        symbols.forEach {
            Text(
                it,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, onDayClick: (Long) -> Unit) {
    val cells = buildList {
        repeat(state.leadingBlanks) { add(null) }
        addAll(state.days)
    }
    cells.chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { day ->
                Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                    if (day != null) DayCell(day, onDayClick)
                }
            }
            repeat(7 - week.size) { Box(Modifier.weight(1f).aspectRatio(1f)) {} }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, onDayClick: (Long) -> Unit) {
    val bg = when {
        day.inPeriod -> MenstrualColor
        day.ovulation -> OvulationColor
        day.fertile -> FertileColor.copy(alpha = 0.55f)
        day.predictedPeriod -> PredictedColor.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val onColor = if (day.inPeriod || day.ovulation) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (day.isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier,
            )
            .clickable { onDayClick(day.epochDay) },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${day.date.dayOfMonth}", color = onColor, style = MaterialTheme.typography.bodyMedium)
            when {
                day.intimacy -> Text("❤️", fontSize = 10.sp)
                day.hasLog -> Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            }
        }
    }
}

@Composable
private fun Legend() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LegendRow(MenstrualColor, stringResource(R.string.legend_period))
        LegendRow(PredictedColor, stringResource(R.string.legend_predicted))
        LegendRow(FertileColor, stringResource(R.string.legend_fertile))
        LegendRow(OvulationColor, stringResource(R.string.legend_ovulation))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("❤️", fontSize = 12.sp)
            Text(stringResource(R.string.legend_intimacy), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun monthLabel(year: Int, month: Int): String =
    java.time.LocalDate.of(year, month, 1)
        .format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))

private fun weekdayInitials(): List<String> {
    // Monday-first week, localized short names.
    val fmt = DateTimeFormatter.ofPattern("EEEEE", Locale.getDefault())
    return (0..6).map { java.time.LocalDate.of(2024, 1, 1).plusDays(it.toLong()).format(fmt) }
}

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
