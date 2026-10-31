package com.splitpay.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `formatInr correctly formats single and double digit amounts`() {
        assertEquals("₹0", CurrencyFormatter.formatInr(0L))
        assertEquals("₹5", CurrencyFormatter.formatInr(5L))
        assertEquals("₹99", CurrencyFormatter.formatInr(99L))
    }

    @Test
    fun `formatInr correctly formats three and four digit amounts`() {
        assertEquals("₹500", CurrencyFormatter.formatInr(500L))
        assertEquals("₹1,500", CurrencyFormatter.formatInr(1500L))
        assertEquals("₹1,999", CurrencyFormatter.formatInr(1999L))
        assertEquals("₹2,000", CurrencyFormatter.formatInr(2000L))
    }

    @Test
    fun `formatInr correctly formats Indian numbering for five and six and seven digit amounts`() {
        assertEquals("₹10,000", CurrencyFormatter.formatInr(10000L))
        assertEquals("₹50,000", CurrencyFormatter.formatInr(50000L))
        assertEquals("₹1,00,000", CurrencyFormatter.formatInr(100000L))
        assertEquals("₹10,00,000", CurrencyFormatter.formatInr(1000000L))
    }
}
