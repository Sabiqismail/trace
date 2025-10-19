package com.trace.journal.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trace.journal.data.EntryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TodayViewModel(
    private val repo: EntryRepository
) : ViewModel() {

    private val _todayText = MutableStateFlow("")
    val todayText: StateFlow<String> = _todayText.asStateFlow()

    init {
        // Initial load + start midnight ticker
        refreshToday()
        startMidnightRefresh()
    }

    /** Public: recompute today's text from repository */
    fun refreshToday() {
        viewModelScope.launch {
            _todayText.value = loadTodayOnce()
        }
    }

    /** Public: save/replace today's entry, then refresh */
    fun saveForToday(text: String) {
        viewModelScope.launch {
            repo.saveFor(LocalDate.now(), text)
            _todayText.value = text // optimistic update
        }
    }

    // ---- Internals ----

    private suspend fun loadTodayOnce(): String {
        val today = LocalDate.now()
        val entries = repo.observeAll().first() // read current snapshot once
        return entries.firstOrNull { it.date == today }?.text.orEmpty()
    }

    private fun startMidnightRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(millisUntilNextMidnight())
                // Day flipped: load today's (now new) entry/text
                _todayText.value = loadTodayOnce()
            }
        }
    }

    private fun millisUntilNextMidnight(): Long {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        return ChronoUnit.MILLIS.between(now, nextMidnight).coerceAtLeast(1000L)
    }

    companion object {
        fun factory(repo: EntryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TodayViewModel(repo) as T
                }
            }
    }
}