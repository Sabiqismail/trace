package com.example.trace.data

import java.time.LocalDate

data class Entry(
    val date: LocalDate,
    val text: String,
    val isHighlighted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)