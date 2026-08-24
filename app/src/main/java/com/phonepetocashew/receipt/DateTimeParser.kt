package com.phonepetocashew.receipt

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

object DateTimeParser {

    // Regex for: "10:43 am on 23 Aug 2026" or "10:43 AM on 23 Aug 2026"
    private val TIME_ON_DATE_REGEX = Regex(
        """([0-9]{1,2}):([0-9]{2})\s*(am|pm|AM|PM)?\s*(?:on|,|at)?\s*([0-9]{1,2})\s+([A-Za-z]{3,9})\s+([0-9]{4})"""
    )

    // Regex for: "23 Aug 2026, 10:43 am" or "23 Aug 2026 at 10:43 am"
    private val DATE_THEN_TIME_REGEX = Regex(
        """([0-9]{1,2})\s+([A-Za-z]{3,9})\s+([0-9]{4})\s*(?:,|at|on)?\s*([0-9]{1,2}):([0-9]{2})\s*(am|pm|AM|PM)?"""
    )

    // Regex for Date only: "23 Aug 2026" or "23 August 2026"
    private val DATE_ONLY_TEXT_REGEX = Regex(
        """\b([0-9]{1,2})\s+([A-Za-z]{3,9})\s+([0-9]{4})\b"""
    )

    // Regex for numeric date: "23/08/2026" or "23-08-2026"
    private val NUMERIC_DATE_REGEX = Regex(
        """\b([0-9]{1,2})[/.-]([0-9]{1,2})[/.-]([0-9]{4})\b"""
    )

    // Regex for ISO format: "2026-08-23"
    private val ISO_DATE_REGEX = Regex(
        """\b([0-9]{4})-([0-9]{2})-([0-9]{2})\b"""
    )

    private val MONTH_MAP = mapOf(
        "jan" to 1, "january" to 1,
        "feb" to 2, "february" to 2,
        "mar" to 3, "march" to 3,
        "apr" to 4, "april" to 4,
        "may" to 5,
        "jun" to 6, "june" to 6,
        "jul" to 7, "july" to 7,
        "aug" to 8, "august" to 8,
        "sep" to 9, "september" to 9, "sept" to 9,
        "oct" to 10, "october" to 10,
        "nov" to 11, "november" to 11,
        "dec" to 12, "december" to 12
    )

    /**
     * Parses a date and time from OCR text.
     * Supports formats like "10:43 am on 23 Aug 2026".
     */
    fun parse(text: String): LocalDateTime? {
        if (text.isBlank()) return null

        val clean = text.trim().replace("\n", " ")

        // 1. Match: "10:43 am on 23 Aug 2026"
        val timeOnDateMatch = TIME_ON_DATE_REGEX.find(clean)
        if (timeOnDateMatch != null) {
            val (hourStr, minStr, amPm, dayStr, monthStr, yearStr) = timeOnDateMatch.destructured
            return buildDateTime(yearStr, monthStr, dayStr, hourStr, minStr, amPm)
        }

        // 2. Match: "23 Aug 2026, 10:43 am"
        val dateThenTimeMatch = DATE_THEN_TIME_REGEX.find(clean)
        if (dateThenTimeMatch != null) {
            val (dayStr, monthStr, yearStr, hourStr, minStr, amPm) = dateThenTimeMatch.destructured
            return buildDateTime(yearStr, monthStr, dayStr, hourStr, minStr, amPm)
        }

        // 3. Match Date only: "23 Aug 2026"
        val dateOnlyMatch = DATE_ONLY_TEXT_REGEX.find(clean)
        if (dateOnlyMatch != null) {
            val (dayStr, monthStr, yearStr) = dateOnlyMatch.destructured
            val month = MONTH_MAP[monthStr.lowercase()] ?: return null
            val day = dayStr.toIntOrNull() ?: return null
            val year = yearStr.toIntOrNull() ?: return null
            return try {
                LocalDate.of(year, month, day).atTime(12, 0)
            } catch (_: Exception) {
                null
            }
        }

        // 4. Match numeric: "23/08/2026"
        val numMatch = NUMERIC_DATE_REGEX.find(clean)
        if (numMatch != null) {
            val (dayStr, monthStr, yearStr) = numMatch.destructured
            val day = dayStr.toIntOrNull() ?: return null
            val month = monthStr.toIntOrNull() ?: return null
            val year = yearStr.toIntOrNull() ?: return null
            return try {
                LocalDate.of(year, month, day).atTime(12, 0)
            } catch (_: Exception) {
                null
            }
        }

        // 5. Match ISO: "2026-08-23"
        val isoMatch = ISO_DATE_REGEX.find(clean)
        if (isoMatch != null) {
            val (yearStr, monthStr, dayStr) = isoMatch.destructured
            val year = yearStr.toIntOrNull() ?: return null
            val month = monthStr.toIntOrNull() ?: return null
            val day = dayStr.toIntOrNull() ?: return null
            return try {
                LocalDate.of(year, month, day).atTime(12, 0)
            } catch (_: Exception) {
                null
            }
        }

        return null
    }

    private fun buildDateTime(
        yearStr: String,
        monthStr: String,
        dayStr: String,
        hourStr: String,
        minStr: String,
        amPm: String?
    ): LocalDateTime? {
        val month = MONTH_MAP[monthStr.lowercase()] ?: return null
        val day = dayStr.toIntOrNull() ?: return null
        val year = yearStr.toIntOrNull() ?: return null

        var hour = hourStr.toIntOrNull() ?: return null
        val minute = minStr.toIntOrNull() ?: return null

        if (amPm != null) {
            if (amPm.equals("pm", ignoreCase = true) && hour < 12) {
                hour += 12
            } else if (amPm.equals("am", ignoreCase = true) && hour == 12) {
                hour = 0
            }
        }

        return try {
            LocalDateTime.of(year, month, day, hour, minute)
        } catch (_: Exception) {
            null
        }
    }
}
