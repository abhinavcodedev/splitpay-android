package com.splitpay.app.util

object CurrencyFormatter {

    /**
     * Formats a Long amount according to the Indian numbering system.
     * Examples:
     * 500 -> "500"
     * 1999 -> "1,999"
     * 10000 -> "10,000"
     * 100000 -> "1,00,000"
     */
    fun formatIndianNumber(amount: Long): String {
        val isNegative = amount < 0
        val str = kotlin.math.abs(amount).toString()
        if (str.length <= 3) {
            return (if (isNegative) "-" else "") + str
        }
        val lastThree = str.takeLast(3)
        val remaining = str.dropLast(3)
        val formattedRemaining = StringBuilder()
        for (i in remaining.indices) {
            if (i > 0 && (remaining.length - i) % 2 == 0) {
                formattedRemaining.append(',')
            }
            formattedRemaining.append(remaining[i])
        }
        return (if (isNegative) "-" else "") + "$formattedRemaining,$lastThree"
    }

    /**
     * Formats a Long amount as INR currency with the ₹ symbol.
     * Examples:
     * 5 -> "₹5"
     * 1999 -> "₹1,999"
     * 10000 -> "₹10,000"
     */
    fun formatInr(amount: Long): String {
        return "₹" + formatIndianNumber(amount)
    }
}
