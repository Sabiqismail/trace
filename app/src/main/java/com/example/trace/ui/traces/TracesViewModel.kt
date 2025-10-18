package com.example.trace.ui.traces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trace.data.Entry
import com.example.trace.data.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TracesViewModel(
    repo: EntryRepository
) : ViewModel() {

    // Stream of all entries, newest first
    val entries: StateFlow<List<Entry>> =
        repo.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    companion object {
        fun factory(repo: EntryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TracesViewModel(repo) as T
                }
            }
    }
}
