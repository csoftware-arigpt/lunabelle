package com.cycletracker.app.ui.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Intensity
import com.cycletracker.app.domain.model.SymptomEntry
import com.cycletracker.app.domain.repository.DailyLogRepository
import com.cycletracker.app.ui.navigation.LogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class LogEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DailyLogRepository,
) : ViewModel() {

    val date: LocalDate = LocalDate.fromEpochDays(savedStateHandle.toRoute<LogEntry>().dateEpochDay.toInt())

    private val _log = MutableStateFlow(DailyLog(date = date))
    val log: StateFlow<DailyLog> = _log

    init {
        viewModelScope.launch {
            repository.observeLog(date).first()?.let { _log.value = it }
        }
    }

    fun update(transform: (DailyLog) -> DailyLog) { _log.value = transform(_log.value) }

    fun isSymptomSelected(code: String): Boolean = _log.value.symptoms.any { it.symptomCode == code }

    fun toggleSymptom(code: String) {
        val current = _log.value.symptoms
        _log.value = _log.value.copy(
            symptoms = if (current.any { it.symptomCode == code }) {
                current.filterNot { it.symptomCode == code }
            } else {
                current + SymptomEntry(code, Intensity.MODERATE)
            },
        )
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.upsertLog(_log.value)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteLog(date)
            onDone()
        }
    }
}
