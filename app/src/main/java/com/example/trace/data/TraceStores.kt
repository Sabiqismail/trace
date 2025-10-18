package com.example.trace.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// A single Preferences DataStore scoped to the app context.
val Context.traceStore: DataStore<Preferences> by preferencesDataStore(name = "trace_store")