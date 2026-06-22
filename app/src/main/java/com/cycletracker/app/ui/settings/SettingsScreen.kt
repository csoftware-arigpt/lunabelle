package com.cycletracker.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cycletracker.app.R
import com.cycletracker.app.core.locale.LocaleManager
import com.cycletracker.app.data.backup.ImportResult
import com.cycletracker.app.domain.model.ReminderPhase
import com.cycletracker.app.domain.model.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val s = settings ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showColorPicker by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val json = viewModel.exportJson(System.currentTimeMillis())
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            Toast.makeText(context, R.string.export_done, Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            val result = content?.let { viewModel.importJson(it) }
            val msg = when (result) {
                is ImportResult.Success -> R.string.import_done
                ImportResult.UnsupportedVersion -> R.string.import_unsupported
                else -> R.string.import_failed
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val onPickLanguage: (String?) -> Unit = { tag ->
        viewModel.setLanguage(tag)
        LocaleManager.persist(context, tag)
        context.findActivity()?.recreate()
    }

    val titleState = remember(s.phaseReminders) {
        mutableStateMapOf<ReminderPhase, String>().apply {
            ReminderPhase.entries.forEach { put(it, s.reminderFor(it).title ?: "") }
        }
    }
    val bodyState = remember(s.phaseReminders) {
        mutableStateMapOf<ReminderPhase, String>().apply {
            ReminderPhase.entries.forEach { put(it, s.reminderFor(it).body ?: "") }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.titleLarge)

        SectionHeader(stringResource(R.string.settings_appearance))
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(s.themeMode == mode, { viewModel.setTheme(mode) }, { Text(stringResource(themeLabel(mode))) })
            }
        }

        Text(stringResource(R.string.settings_theme_color), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(s.seedColor == 0L, { viewModel.setSeedColor(0L) }, { Text(stringResource(R.string.settings_theme_color_default)) })
            THEME_SEEDS.forEach { seed ->
                ColorSwatch(Color(seed), s.seedColor == seed) { viewModel.setSeedColor(seed) }
            }
        }
        OutlinedButton(onClick = { showColorPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_theme_color_custom))
        }
        if (showColorPicker) {
            ColorPickerDialog(
                initial = if (s.seedColor != 0L) Color(s.seedColor) else MaterialTheme.colorScheme.primary,
                onDismiss = { showColorPicker = false },
                onConfirm = { viewModel.setSeedColor(it); showColorPicker = false },
            )
        }

        SectionHeader(stringResource(R.string.settings_language))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(s.localeTag == null, { onPickLanguage(null) }, { Text(stringResource(R.string.lang_system)) })
            FilterChip(s.localeTag == "ru", { onPickLanguage("ru") }, { Text(stringResource(R.string.lang_ru)) })
            FilterChip(s.localeTag == "en", { onPickLanguage("en") }, { Text(stringResource(R.string.lang_en)) })
        }

        SectionHeader(stringResource(R.string.settings_reminders))
        ReminderPhase.entries.forEach { phase ->
            val reminder = s.reminderFor(phase)
            ToggleRow(stringResource(phaseLabel(phase)), reminder.enabled) { viewModel.setPhaseEnabled(phase, it) }
            if (reminder.enabled) {
                if (phase == ReminderPhase.MENSTRUAL) {
                    Stepper(stringResource(R.string.settings_reminder_lead), s.periodReminderLeadDays, 0, 14, viewModel::setPeriodLeadDays)
                }
                OutlinedTextField(
                    value = titleState[phase] ?: "", onValueChange = { titleState[phase] = it },
                    label = { Text(stringResource(R.string.settings_custom_title)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = bodyState[phase] ?: "", onValueChange = { bodyState[phase] = it },
                    label = { Text(stringResource(R.string.settings_custom_text)) },
                    supportingText = { Text(stringResource(R.string.settings_custom_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (ReminderPhase.entries.any { s.reminderFor(it).enabled }) {
            Button(
                onClick = {
                    viewModel.savePhaseTexts(titleState.toMap(), bodyState.toMap())
                    Toast.makeText(context, R.string.reminders_saved, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_save_reminders)) }
        }

        SectionHeader(stringResource(R.string.settings_privacy))
        ToggleRow(stringResource(R.string.settings_app_lock), s.appLockEnabled, viewModel::setAppLock)

        SectionHeader(stringResource(R.string.settings_data))
        OutlinedButton(onClick = { exportLauncher.launch("cycle-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_export))
        }
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_import))
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(
            stringResource(R.string.settings_about),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().clickable { onOpenAbout() }.padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }) { Text("−") }
        Text("$value", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }) { Text("+") }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun themeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

private fun phaseLabel(phase: ReminderPhase): Int = when (phase) {
    ReminderPhase.MENSTRUAL -> R.string.rem_phase_menstrual
    ReminderPhase.FOLLICULAR -> R.string.rem_phase_follicular
    ReminderPhase.OVULATORY -> R.string.rem_phase_ovulatory
    ReminderPhase.LUTEAL -> R.string.rem_phase_luteal
}

private val THEME_SEEDS = listOf(
    0xFFEC4899L, 0xFFE11D6BL, 0xFFD81B8CL, 0xFFB4345FL,
    0xFF8B5CF6L, 0xFFA78BFAL, 0xFFFF6B6BL, 0xFFFF9472L,
    0xFF14B8A6L, 0xFF34D399L, 0xFF3B82F6L, 0xFF38BDF8L,
)

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
    }
}
