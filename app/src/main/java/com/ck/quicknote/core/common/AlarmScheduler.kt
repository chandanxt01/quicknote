package com.ck.quicknote.core.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.feature.note_detail.AlarmReceiver

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(note: Note) {
        if (note.reminder == null) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_NOTE_ID", note.id)
            putExtra("EXTRA_TITLE", note.title)
            putExtra("EXTRA_MESSAGE", note.content.take(50)) // Show first 50 chars
        }

        // Unique Request Code using Note ID (hashcode if null)
        val requestCode = note.id ?: note.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                note.reminder,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle permission exception if exact alarm permission is missing
            e.printStackTrace()
        }
    }

    fun cancel(note: Note) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = note.id ?: note.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}