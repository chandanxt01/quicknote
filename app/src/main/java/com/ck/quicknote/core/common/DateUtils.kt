package com.ck.quicknote.core.common

import android.text.format.DateUtils

object DateUtils {
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        // Android ka built-in helper jo "2 mins ago" jaisa string deta hai
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}