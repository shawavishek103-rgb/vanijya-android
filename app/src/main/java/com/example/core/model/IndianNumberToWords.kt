package com.example.core.model

import kotlin.math.abs

/**
 * Converts Money or integer rupee values into Indian numbering system words
 * (Crore, Lakh, Thousand, Hundred, and Paise) for GST invoices and payslips.
 */
object IndianNumberToWords {

    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    /**
     * Converts a [Money] amount into formal Indian currency words, e.g.:
     * `Money.fromRupeesPaise(12_34_567, 50)` ->
     * `"Rupees Twelve Lakh Thirty-Four Thousand Five Hundred Sixty-Seven and Paise Fifty Only"`
     */
    fun toCurrencyWords(money: Money): String {
        if (money.isZero) return "Rupees Zero Only"

        val negativePrefix = if (money.isNegative) "Minus " else ""
        val absPaise = abs(money.paise)
        val rupees = absPaise / 100L
        val paise = absPaise % 100L

        val rupeeWords = if (rupees > 0L) {
            "Rupees ${convertWholeNumber(rupees)}"
        } else {
            ""
        }

        val paiseWords = if (paise > 0L) {
            "Paise ${convertBelowHundred(paise.toInt())}"
        } else {
            ""
        }

        val body = when {
            rupeeWords.isNotEmpty() && paiseWords.isNotEmpty() -> "$rupeeWords and $paiseWords"
            rupeeWords.isNotEmpty() -> rupeeWords
            else -> paiseWords
        }

        return "$negativePrefix$body Only"
    }

    /**
     * Converts a non-negative integer into Indian words (Arab, Crore, Lakh, Thousand, Hundred).
     */
    fun convertWholeNumber(number: Long): String {
        if (number == 0L) return "Zero"
        val n = abs(number)

        val parts = mutableListOf<String>()

        val crorePart = n / 1_00_00_000L
        val afterCrore = n % 1_00_00_000L

        if (crorePart > 0) {
            // Recursive support for > 99 Crore (e.g., 150 Crore -> One Hundred Fifty Crore)
            parts.add("${convertWholeNumber(crorePart)} Crore")
        }

        val lakhPart = (afterCrore / 1_00_000L).toInt()
        val afterLakh = afterCrore % 1_00_000L
        if (lakhPart > 0) {
            parts.add("${convertBelowHundred(lakhPart)} Lakh")
        }

        val thousandPart = (afterLakh / 1_000L).toInt()
        val afterThousand = afterLakh % 1_000L
        if (thousandPart > 0) {
            parts.add("${convertBelowHundred(thousandPart)} Thousand")
        }

        val hundredPart = (afterThousand / 100L).toInt()
        val remainder = (afterThousand % 100L).toInt()
        if (hundredPart > 0) {
            parts.add("${units[hundredPart]} Hundred")
        }

        if (remainder > 0) {
            parts.add(convertBelowHundred(remainder))
        }

        return parts.joinToString(" ")
    }

    private fun convertBelowHundred(n: Int): String {
        require(n in 0..99)
        return when {
            n < 20 -> units[n]
            n % 10 == 0 -> tens[n / 10]
            else -> "${tens[n / 10]}-${units[n % 10]}"
        }
    }
}
