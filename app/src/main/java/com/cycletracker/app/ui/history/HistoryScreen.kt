package com.cycletracker.app.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cycletracker.app.R
import com.cycletracker.app.ui.common.SYMPTOM_LABELS
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.hasData) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("📊", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val stats = state.stats
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    stringResource(R.string.stat_avg_cycle), stats?.meanLength?.roundToInt()?.toString() ?: "—",
                    MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f),
                )
                StatCard(
                    stringResource(R.string.stat_avg_period), stats?.meanPeriodLength?.roundToInt()?.toString() ?: "—",
                    MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(1f),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    stringResource(R.string.stat_range),
                    if (stats?.minLength != null && stats.maxLength != null) "${stats.minLength}–${stats.maxLength}" else "—",
                    MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f),
                )
                val irregular = stats?.isIrregular == true
                StatCard(
                    stringResource(R.string.stat_regularity),
                    stringResource(if (irregular) R.string.regularity_irregular else R.string.regularity_regular),
                    if (irregular) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    if (irregular) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    Modifier.weight(1f),
                )
            }
        }
        if (state.cycleLengths.size >= 2) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.chart_cycle_lengths), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        CycleLengthChart(
                            state.cycleLengths.takeLast(12),
                            barColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().height(140.dp).padding(top = 16.dp),
                        )
                    }
                }
            }
        }
        if (state.symptomFrequency.isNotEmpty()) {
            item { Text(stringResource(R.string.history_top_symptoms), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(state.symptomFrequency.size) { i ->
                val (code, count) = state.symptomFrequency[i]
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(SYMPTOM_LABELS[code]?.let { stringResource(it) } ?: code, style = MaterialTheme.typography.bodyLarge)
                    Text("$count", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, container: Color, onContainer: Color, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CycleLengthChart(values: List<Int>, barColor: Color, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    val maxV = values.max()
    val minV = values.min()
    val lo = (minV - 2).coerceAtLeast(0)
    val span = (maxV + 2 - lo).coerceAtLeast(1)
    Canvas(modifier) {
        val n = values.size
        val gap = 10.dp.toPx()
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtMost(48.dp.toPx())
        val totalW = barW * n + gap * (n - 1)
        val startX = (size.width - totalW) / 2f
        val r = CornerRadius(barW / 3f, barW / 3f)
        values.forEachIndexed { i, v ->
            val h = size.height * ((v - lo).toFloat() / span)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(startX + i * (barW + gap), size.height - h),
                size = Size(barW, h),
                cornerRadius = r,
            )
        }
    }
}
