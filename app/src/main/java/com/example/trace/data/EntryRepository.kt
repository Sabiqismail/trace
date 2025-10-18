package com.example.trace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlinx.serialization.builtins.ListSerializer

class EntryRepository(
    private val store: DataStore<Preferences>
) {
    private val KEY_JSON = stringPreferencesKey("entries_json")
    private val listSerializer = ListSerializer(EntryDTO.serializer())

    // Observe all entries (newest first)
    fun observeAll(): Flow<List<Entry>> =
        store.data.map { prefs ->
            val json = prefs[KEY_JSON]
            if (json.isNullOrBlank()) emptyList()
            else TraceJson.decodeFromString(listSerializer, json).map { it.toEntry() }
        }.map { list ->
            list.sortedByDescending { it.date }
        }

    // Internal: read current list once
    private suspend fun readOnce(): MutableList<Entry> =
        withContext(Dispatchers.IO) {
            val prefs = store.data.first()
            val json = prefs[KEY_JSON]
            val list = if (json.isNullOrBlank()) emptyList()
            else TraceJson.decodeFromString(listSerializer, json).map { it.toEntry() }
            list.toMutableList()
        }

    // Save or replace an entry for a given date
    suspend fun saveFor(date: LocalDate, text: String) {
        val now = System.currentTimeMillis()
        val items = readOnce()
        val idx = items.indexOfFirst { it.date == date }
        val newEntry = if (idx >= 0) {
            val prev = items[idx]
            prev.copy(text = text, updatedAt = now)
        } else {
            Entry(date = date, text = text, createdAt = now, updatedAt = now)
        }
        if (idx >= 0) items[idx] = newEntry else items.add(newEntry)
        write(items)
    }

    suspend fun toggleHighlight(date: LocalDate, highlight: Boolean) {
        val now = System.currentTimeMillis()
        val items = readOnce()
        val idx = items.indexOfFirst { it.date == date }
        if (idx >= 0) {
            items[idx] = items[idx].copy(isHighlighted = highlight, updatedAt = now)
            write(items)
        }
    }

    suspend fun delete(date: LocalDate) {
        val items = readOnce()
        items.removeAll { it.date == date }
        write(items)
    }

    private suspend fun write(items: List<Entry>) {
        val json = TraceJson.encodeToString(listSerializer, items.map { it.toDTO() })
        store.edit { it[KEY_JSON] = json }
    }
}

// Tiny helper to build the repo from a Context
fun entryRepository(context: Context) = EntryRepository(context.traceStore)