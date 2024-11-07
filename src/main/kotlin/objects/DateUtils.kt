package objects

import models.TaskModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // Define common date format patterns
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

    // Create thread-safe date formatters
    private val dateFormatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat(DATE_FORMAT)
        }
    }

    private val dateTimeFormatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat(DATE_TIME_FORMAT)
        }
    }

    /**
     * Converts a date string to milliseconds timestamp
     * Accepts formats: "yyyy-MM-dd" or "yyyy-MM-dd HH:mm"
     * @param dateStr the date string to parse
     * @return milliseconds since epoch, or null if parsing fails
     */
    fun parseDeadline(dateStr: String): Long? {
        if (dateStr.isBlank()) return null

        return try {
            // Try parsing with date-time format first
            if (dateStr.contains(":")) {
                dateTimeFormatter.get()?.parse(dateStr)?.time
            } else {
                // If no time component, parse as date only and set time to end of day
                dateFormatter.get()?.parse(dateStr)?.apply {
                    // Set time to 23:59:59
                    hours = 23
                    minutes = 59
                    seconds = 59
                }?.time
            }
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * Formats a timestamp into a human-readable date string
     * @param timestamp milliseconds since epoch
     * @param includeTime whether to include time in the output
     * @return formatted date string or null if timestamp is null
     */
    fun formatDeadline(timestamp: Long?, includeTime: Boolean = false): String? {
        if (timestamp == null) return null

        return try {
            val date = Date(timestamp)
            if (includeTime) {
                dateTimeFormatter.get()?.format(date)
            } else {
                dateFormatter.get()?.format(date)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates if a date string is in the correct format
     * @param dateStr the date string to validate
     * @return true if the date string is valid
     */
    fun isValidDateFormat(dateStr: String): Boolean {
        return try {
            if (dateStr.contains(":")) {
                dateTimeFormatter.get()?.parse(dateStr) != null
            } else {
                dateFormatter.get()?.parse(dateStr) != null
            }
        } catch (e: ParseException) {
            false
        }
    }
}
