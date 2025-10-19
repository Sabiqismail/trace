package com.trace.journal.data

import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EntryDTO(
    val dateEpochDay: Long,
    val text: String,
    val isHighlighted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun Entry.toDTO(): EntryDTO = EntryDTO(
    dateEpochDay = date.toEpochDay(),
    text = text,
    isHighlighted = isHighlighted,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun EntryDTO.toEntry(): Entry = Entry(
    date = LocalDate.ofEpochDay(dateEpochDay),
    text = text,
    isHighlighted = isHighlighted,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// A single, lenient Json instance
val TraceJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}