package com.example.trace.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trace.data.EntryRepository
import java.time.LocalDate
import kotlinx.coroutines.launch

class TodayViewModel(
    private val repo: EntryRepository
) : ViewModel() {

    // Called by the UI to save today's thought
    fun saveForToday(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repo.saveFor(LocalDate.now(), trimmed)
        }
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
