package com.phonepetocashew.receipt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class DateTimeParserTest {

    @Test
    fun parse_standardPhonePeHeaderFormat() {
        val result = DateTimeParser.parse("10:43 am on 23 Aug 2026")
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 43), result)
    }

    @Test
    fun parse_withCapitalAmPm() {
        val result = DateTimeParser.parse("10:43 AM on 23 Aug 2026")
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 43), result)

        val pmResult = DateTimeParser.parse("04:15 PM on 15 Oct 2025")
        assertNotNull(pmResult)
        assertEquals(LocalDateTime.of(2025, 10, 15, 16, 15), pmResult)
    }

    @Test
    fun parse_midnightAndNoon() {
        val midnight = DateTimeParser.parse("12:00 am on 1 Jan 2026")
        assertNotNull(midnight)
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), midnight)

        val noon = DateTimeParser.parse("12:30 pm on 1 Jan 2026")
        assertNotNull(noon)
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 30), noon)
    }

    @Test
    fun parse_dateOnly() {
        val dateOnly = DateTimeParser.parse("23 Aug 2026")
        assertNotNull(dateOnly)
        assertEquals(2026, dateOnly?.year)
        assertEquals(8, dateOnly?.monthValue)
        assertEquals(23, dateOnly?.dayOfMonth)
    }

    @Test
    fun parse_isoDate() {
        val iso = DateTimeParser.parse("2026-08-23")
        assertNotNull(iso)
        assertEquals(2026, iso?.year)
        assertEquals(8, iso?.monthValue)
        assertEquals(23, iso?.dayOfMonth)
    }

    @Test
    fun parse_numericDate() {
        val num = DateTimeParser.parse("23/08/2026")
        assertNotNull(num)
        assertEquals(2026, num?.year)
        assertEquals(8, num?.monthValue)
        assertEquals(23, num?.dayOfMonth)
    }

    @Test
    fun parse_invalidReturnsNull() {
        assertNull(DateTimeParser.parse(""))
        assertNull(DateTimeParser.parse("Paid to Merchant"))
        assertNull(DateTimeParser.parse("₹30.00"))
    }
}
