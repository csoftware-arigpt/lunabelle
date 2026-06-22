package com.cycletracker.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cycletracker.app.R
import com.cycletracker.app.core.designsystem.expressive.CycleDayRing
import com.cycletracker.app.core.designsystem.theme.MenstrualColor
import com.cycletracker.app.ui.common.color
import com.cycletracker.app.ui.common.emoji
import com.cycletracker.app.ui.common.formatMedium
import com.cycletracker.app.ui.common.labelRes

@Composable
fun TodayScreen(
    onQuickLog: (Long) -> Unit,
    onAddPeriod: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    when {
        state.loading -> Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        !state.hasData -> EmptyToday(onAddPeriod)
        else -> TodayContent(
            state = state,
            onQuickLog = onQuickLog,
            onPeriodStarted = viewModel::startPeriodToday,
            onContinuePeriod = viewModel::continuePeriod,
            onCancel = { asDelay ->
                viewModel.cancelPeriod(asDelay)
                Toast.makeText(
                    context,
                    if (asDelay) R.string.cancel_registered_delay else R.string.cancel_removed,
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onMarkDelay = { Toast.makeText(context, R.string.delay_marked, Toast.LENGTH_SHORT).show() },
        )
    }
}

@Composable
private fun EmptyToday(onLog: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🦄", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.today_empty_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.today_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLog) { Text(stringResource(R.string.today_empty_cta)) }
    }
}

@Composable
private fun TodayContent(
    state: TodayUiState,
    onQuickLog: (Long) -> Unit,
    onPeriodStarted: () -> Unit,
    onContinuePeriod: () -> Unit,
    onCancel: (Boolean) -> Unit,
    onMarkDelay: () -> Unit,
) {
    val phaseColor = state.phase.color()
    val delayed = state.delayDays != null
    val ringColor = if (delayed) MenstrualColor else phaseColor
    var dialog by remember { mutableStateOf<TodayDialog?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                stringResource(R.string.tab_today) + "  🦄",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(252.dp).clip(CircleShape).background(ringColor.copy(alpha = 0.12f)))
                CycleDayRing(progress = if (delayed) 1f else state.ringProgress, color = ringColor) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (delayed) {
                            Text("⏳", fontSize = 34.sp)
                            Text("${state.delayDays}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = ringColor)
                            Text(stringResource(R.string.today_delay_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(state.phase.emoji(), fontSize = 34.sp)
                            Text("${state.cycleDay}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = ringColor)
                            Text(stringResource(R.string.today_day_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(state.phase.labelRes()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ringColor)
                        }
                    }
                }
            }
        }
        item {
            com.cycletracker.app.core.designsystem.expressive.PhaseProgressBar(
                progress = if (delayed) 1f else state.ringProgress,
                color = ringColor,
                modifier = Modifier.fillMaxWidth(0.78f).height(10.dp),
            )
        }
        item {
            Button(
                onClick = { dialog = if (state.onPeriod) TodayDialog.CONTINUE else TodayDialog.START },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MenstrualColor, contentColor = Color.White),
            ) {
                Text(
                    "🩸  " + stringResource(if (state.onPeriod) R.string.action_period_continue else R.string.action_period_started),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (state.onPeriod) {
            item {
                OutlinedButton(onClick = { dialog = TodayDialog.CANCEL }, modifier = Modifier.fillMaxWidth()) {
                    Text("✖  " + stringResource(R.string.action_period_cancel))
                }
            }
        }
        state.delayDays?.let { d ->
            item {
                FilledTonalButton(
                    onClick = onMarkDelay,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(
                        "⏳  " + stringResource(R.string.card_delay) + " · " + stringResource(R.string.delay_days_fmt, d),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        state.daysUntilNextPeriod?.let { days ->
            item {
                CuteCard(
                    emoji = "🩸",
                    title = stringResource(R.string.card_next_period),
                    primary = if (days <= 0) stringResource(R.string.due_today) else pluralStringResource(R.plurals.in_days, days, days),
                    secondary = state.nextPeriod?.let { stringResource(R.string.expected_window, it.rangeStart.formatMedium(), it.rangeEnd.formatMedium()) }.orEmpty(),
                    container = MaterialTheme.colorScheme.primaryContainer,
                    onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        state.fertileWindow?.let { fw ->
            item {
                CuteCard(
                    emoji = "💗",
                    title = stringResource(R.string.card_fertile_window),
                    primary = "${fw.start.formatMedium()} – ${fw.end.formatMedium()}",
                    secondary = state.ovulation?.let { stringResource(R.string.ovulation_on, it.day.formatMedium()) }.orEmpty(),
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
        if (state.insights.isNotEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💡 " + stringResource(R.string.section_insights), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        state.insights.forEach { Text(stringResource(it.type.labelRes()), style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
        item {
            Button(onClick = { onQuickLog(state.todayEpochDay) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(stringResource(R.string.action_log_today), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    when (dialog) {
        TodayDialog.START -> ConfirmDialog(
            text = stringResource(R.string.confirm_period_started),
            onConfirm = { onPeriodStarted(); dialog = null },
            onDismiss = { dialog = null },
        )
        TodayDialog.CONTINUE -> ConfirmDialog(
            text = stringResource(R.string.confirm_period_continue),
            onConfirm = { onContinuePeriod(); dialog = null },
            onDismiss = { dialog = null },
        )
        TodayDialog.CANCEL -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.confirm_cancel_title)) },
            text = { Text(stringResource(R.string.confirm_cancel_reason)) },
            confirmButton = { TextButton(onClick = { onCancel(true); dialog = null }) { Text(stringResource(R.string.reason_delay)) } },
            dismissButton = { TextButton(onClick = { onCancel(false); dialog = null }) { Text(stringResource(R.string.reason_mistake)) } },
        )
        null -> {}
    }
}

private enum class TodayDialog { START, CONTINUE, CANCEL }

@Composable
private fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.color_cancel)) } },
    )
}

@Composable
private fun CuteCard(emoji: String, title: String, primary: String, secondary: String, container: Color, onContainer: Color) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$emoji  $title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (secondary.isNotEmpty()) {
                Text(secondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
