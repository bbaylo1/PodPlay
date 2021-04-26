package com.brandonbaylosis.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // This method converts the date returned from iTunes into a simple month, date and
    // year format using the user’s current locale
    fun jsonDateToShortDate(jsonDate: String?): String {
        //1 Checks that jsonDate string coming in isn't null
        if (jsonDate == null) {
            // Returns "-" if it is, to indicate that no date was provided
            return "-"
        }
        // 2 Define a SimpleDateFormat to match the date format returned by iTunes.
        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault())
        // 3 Parse jsonDate string and place it into a Date object named date
        val date = inFormat.parse(jsonDate) ?: return "-"
        // 4 Output format is defined as a short date to match the currently defined locale
        val outputFormat = DateFormat.getDateInstance(DateFormat.SHORT,
            Locale.getDefault())
        // 5 Format the date and return it
        return outputFormat.format(date)
    }

    // Converts date string found in RSS xml feed to date object
    fun xmlDateToDate(dateString: String?): Date {
        val date = dateString ?: return Date()
        val inFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
                Locale.getDefault())
        return inFormat.parse(date) ?: Date()
    }

    // Creates locale-aware short date string
    fun dateToShortDate(date: Date): String {
        val outputFormat = DateFormat.getDateInstance(
            DateFormat.SHORT, Locale.getDefault())
        return outputFormat.format(date)
    }
}