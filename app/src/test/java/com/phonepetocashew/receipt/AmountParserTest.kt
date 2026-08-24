package com.phonepetocashew.receipt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {

    @Test
    fun parse_withRupeeSymbol() {
        assertEquals(30.0, AmountParser.parse("₹30")!!, 0.001)
        assertEquals(347.0, AmountParser.parse("₹347")!!, 0.001)
        assertEquals(347.0, AmountParser.parse("₹347.00")!!, 0.001)
        assertEquals(123456.78, AmountParser.parse("₹1,23,456.78")!!, 0.001)
        assertEquals(500.50, AmountParser.parse("₹ 500.50")!!, 0.001)
    }

    @Test
    fun parse_withRsPrefix() {
        assertEquals(347.0, AmountParser.parse("Rs. 347")!!, 0.001)
        assertEquals(347.0, AmountParser.parse("Rs 347")!!, 0.001)
        assertEquals(347.50, AmountParser.parse("Rs. 347.50")!!, 0.001)
        assertEquals(1500.0, AmountParser.parse("INR 1500")!!, 0.001)
    }

    @Test
    fun parse_decimalNumbers() {
        assertEquals(347.0, AmountParser.parse("347.00")!!, 0.001)
        assertEquals(12.50, AmountParser.parse("12.50")!!, 0.001)
        assertEquals(1050.25, AmountParser.parse("1,050.25")!!, 0.001)
    }

    @Test
    fun parse_standaloneCropNumbers() {
        assertEquals(30.0, AmountParser.parse("30")!!, 0.001)
        assertEquals(500.0, AmountParser.parse("500")!!, 0.001)
    }

    @Test
    fun parse_ignoresUtrAndTxnId() {
        // UTR is a 12 digit number with no decimal point
        assertNull(AmountParser.parse("123456789012"))
        assertNull(AmountParser.parse("UTR: 123456789012"))
        assertNull(AmountParser.parse("T2400000000000000000001"))
    }

    @Test
    fun parse_handlesWhitespaceAndLines() {
        val multiline = """
            Paid to
            Merchant Store
            ₹30
        """.trimIndent()
        assertEquals(30.0, AmountParser.parse(multiline)!!, 0.001)
    }

    @Test
    fun parse_withOcrMisreadRupeeSymbolAs7() {
        assertEquals(30.0, AmountParser.parse("7 30")!!, 0.001)
        assertEquals(347.0, AmountParser.parse("7 347")!!, 0.001)
        assertEquals(500.50, AmountParser.parse("7 500.50")!!, 0.001)
        assertEquals(1234.0, AmountParser.parse("7 1,234")!!, 0.001)
        assertEquals(30.0, AmountParser.parse("7\n30")!!, 0.001)
        assertEquals(500.0, AmountParser.parse("7\n500")!!, 0.001)
    }

    @Test
    fun parse_withOcrMisreadSymbols() {
        assertEquals(30.0, AmountParser.parse("? 30")!!, 0.001)
        assertEquals(30.0, AmountParser.parse("?30")!!, 0.001)
        assertEquals(347.0, AmountParser.parse("?\n347")!!, 0.001)
        assertEquals(50.0, AmountParser.parse("> 50")!!, 0.001)
    }

    @Test
    fun parse_invalidOrEmptyReturnsNull() {
        assertNull(AmountParser.parse(""))
        assertNull(AmountParser.parse("   "))
        assertNull(AmountParser.parse("Transaction Successful"))
        assertNull(AmountParser.parse("merchant@upi"))
    }
}
