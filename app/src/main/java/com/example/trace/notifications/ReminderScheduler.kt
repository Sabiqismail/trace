package com.example.trace.notifications

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val UNIQUE_NAME = "trace.daily.reminder"

    /**
     * Schedule a daily reminder at [hour24]:[minute] device local time.
     * Example: scheduleDaily(context, 21, 0) -> 9:00 PM daily
     */
    fun scheduleDaily(context: Context, hour24: Int, minute: Int) {
        val initialDelay = computeInitialDelayMillis(hour24, minute)

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Cancel the daily reminder (turn it off). */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }

    /** Compute delay from now until the next occurrence of hour:minute (local time). */
    private fun computeInitialDelayMillis(hour24: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }
        // If the time today has already passed, move to tomorrow
        if (next.timeInMillis <= now.timeInMillis) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}