package com.example.trace.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trace.data.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(
    private val repo: EntryRepository
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    // Stream of today's text ("" if none) — used to prefill the input
    val todayText: StateFlow<String> =
        repo.observeFor(today)
            .map { it?.text ?: "" }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ""
            )

    // Save/replace ONLY today's entry
    fun saveForToday(text: String) {
        viewModelScope.launch {
            repo.saveFor(today, text)
        }
    }

    companion object {
        fun factory(repo: EntryRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodayViewModel(repo) as T
            }
        }
    }
}