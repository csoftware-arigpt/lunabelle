package com.cycletracker.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cycletracker.app.R
import com.cycletracker.app.domain.model.GoalMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var cycleLength by remember { mutableIntStateOf(28) }
    var periodLength by remember { mutableIntStateOf(5) }
    var goalMode by remember { mutableStateOf(GoalMode.TRACKING_ONLY) }
    var accepted by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.onboarding_welcome_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Stepper(stringResource(R.string.onboarding_cycle_length), cycleLength, 21, 40) { cycleLength = it }
        Stepper(stringResource(R.string.onboarding_period_length), periodLength, 2, 10) { periodLength = it }

        Text(stringResource(R.string.onboarding_goal), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalMode.entries.forEach { mode ->
                FilterChip(selected = goalMode == mode, onClick = { goalMode = mode }, label = { Text(stringResource(goalLabel(mode))) })
            }
        }

        Text(stringResource(R.string.disclaimer_contra_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.disclaimer_contra_body), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.disclaimer_estimates_body), style = MaterialTheme.typography.bodySmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
            Text(stringResource(R.string.onboarding_accept), style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = { viewModel.complete(cycleLength, periodLength, goalMode, null, onComplete) },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.onboarding_start)) }
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }) { Text("−") }
        Text("$value", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }) { Text("+") }
    }
}

private fun goalLabel(mode: GoalMode): Int = when (mode) {
    GoalMode.TRACKING_ONLY -> R.string.goal_tracking
    GoalMode.TRYING_TO_CONCEIVE -> R.string.goal_ttc
    GoalMode.AVOID_PREGNANCY_INFO -> R.string.goal_avoid
}
