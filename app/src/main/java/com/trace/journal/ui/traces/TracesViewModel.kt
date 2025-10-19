package com.trace.journal.ui.traces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trace.journal.data.Entry
import com.trace.journal.data.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TracesViewModel(private val repo: EntryRepository) : ViewModel() {

    val entries: StateFlow<List<Entry>> =
        repo.observeAll()
            .map { it } // Already sorted in the repository
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- highlight toggle
    fun toggleHighlight(date: LocalDate, highlight: Boolean) {
        viewModelScope.launch {
            repo.toggleHighlight(date, highlight)
        }
    }

    // --- delete entry
    fun delete(date: LocalDate) {
        viewModelScope.launch {
            repo.delete(date)
        }
    }

    companion object {
        fun factory(repo: EntryRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TracesViewModel(repo) as T
            }
        }
    }
}