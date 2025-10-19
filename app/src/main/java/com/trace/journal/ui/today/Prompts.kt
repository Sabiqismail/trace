package com.trace.journal.ui.today

import java.time.LocalDate

object Prompts {
    private val list = listOf(
        "What stayed with you today?",
        "What softened you?",
        "What surprised you?",
        "What challenged you gently?",
        "What felt alive?",
        "What did you learn quietly?",
        "What did you let go of?",
        "What felt enough today?",
        "What felt like calm?",
        "What deserves to be remembered?"
    )

    fun forDate(date: LocalDate = LocalDate.now()): String {
        // Stable pseudo-random rotation — same prompt every given day
        val index = (date.toEpochDay().toInt() % list.size).absoluteValue
        return list[index]
    }

    private val Int.absoluteValue: Int
        get() = if (this < 0) -this else this
}