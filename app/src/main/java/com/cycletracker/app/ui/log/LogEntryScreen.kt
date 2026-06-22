package com.cycletracker.app.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cycletracker.app.R
import com.cycletracker.app.domain.model.CervicalMucus
import com.cycletracker.app.domain.model.FlowLevel
import com.cycletracker.app.domain.model.LhTest
import com.cycletracker.app.domain.model.SexualActivity
import com.cycletracker.app.ui.common.SYMPTOM_CATALOG
import com.cycletracker.app.ui.common.formatMedium
import com.cycletracker.app.ui.common.labelRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogEntryScreen(
    dateEpochDay: Long,
    onBack: () -> Unit,
    viewModel: LogEntryViewModel = hiltViewModel(),
) {
    val log by viewModel.log.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.date.formatMedium()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(stringResource(R.string.log_flow))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowLevel.entries.forEach { level ->
                    FilterChip(
                        selected = log.flow == level,
                        onClick = { viewModel.update { it.copy(flow = level) } },
                        label = { Text(stringResource(level.labelRes())) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.log_symptoms))
            SYMPTOM_CATALOG.forEach { category ->
                Text(stringResource(category.titleRes), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    category.symptoms.forEach { def ->
                        FilterChip(
                            selected = viewModel.isSymptomSelected(def.code),
                            onClick = { viewModel.toggleSymptom(def.code) },
                            label = { Text(stringResource(def.labelRes)) },
                        )
                    }
                }
            }

            SectionTitle(stringResource(R.string.log_mucus))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CervicalMucus.entries.forEach { m ->
                    FilterChip(
                        selected = log.cervicalMucus == m,
                        onClick = { viewModel.update { it.copy(cervicalMucus = m) } },
                        label = { Text(stringResource(m.labelRes())) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.log_lh))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LhTest.entries.forEach { t ->
                    FilterChip(
                        selected = log.lhTest == t,
                        onClick = { viewModel.update { it.copy(lhTest = t) } },
                        label = { Text(stringResource(t.labelRes())) },
                    )
                }
            }

            SectionTitle("❤️ " + stringResource(R.string.log_activity))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SexualActivity.entries.forEach { a ->
                    FilterChip(
                        selected = log.sexualActivity == a,
                        onClick = { viewModel.update { it.copy(sexualActivity = a) } },
                        label = { Text(stringResource(a.labelRes())) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.log_notes))
            OutlinedTextField(
                value = log.notes.orEmpty(),
                onValueChange = { text -> viewModel.update { it.copy(notes = text.ifBlank { null }) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Button(onClick = { viewModel.save(onBack) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(R.string.action_save))
            }
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(R.string.log_delete))
            }
            if (showDelete) {
                AlertDialog(
                    onDismissRequest = { showDelete = false },
                    title = { Text(stringResource(R.string.log_delete)) },
                    text = { Text(stringResource(R.string.confirm_delete_log)) },
                    confirmButton = { TextButton(onClick = { showDelete = false; viewModel.delete(onBack) }) { Text(stringResource(R.string.action_confirm)) } },
                    dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.color_cancel)) } },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}
