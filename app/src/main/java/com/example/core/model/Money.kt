package com.example.core.model

import kotlin.math.abs

/**
 * Immutable value class representing monetary amounts strictly in integer paise (1 Rupee = 100 Paise).
 * Floating-point types (Float/Double) are never used for storage or arithmetic.
 */
@JvmInline
value class Money(val paise: Long) : Comparable<Money> {

    val isZero: Boolean get() = paise == 0L
    val isPositive: Boolean get() = paise > 0L
    val isNegative: Boolean get() = paise < 0L

    val rupeesPart: Long get() = paise / 100L
    val paisePart: Long get() = abs(paise % 100L)

    operator fun plus(other: Money): Money = Money(Math.addExact(this.paise, other.paise))
    operator fun minus(other: Money): Money = Money(Math.subtractExact(this.paise, other.paise))
    operator fun unaryMinus(): Money = Money(-this.paise)

    operator fun times(quantity: Int): Money = Money(Math.multiplyExact(this.paise, quantity.toLong()))
    operator fun times(quantity: Long): Money = Money(Math.multiplyExact(this.paise, quantity))

    /**
     * Multiplies by basis points (1% = 100 bps, 18% = 1800 bps, 0.25% = 25 bps) using half-up integer rounding.
     */
    fun percentageBasisPoints(bps: Int): Money {
        val product = this.paise * bps.toLong()
        val sign = if (product >= 0) 1L else -1L
        val rounded = (abs(product) + 5_000L) / 10_000L
        return Money(sign * rounded)
    }

    /**
     * Rounds to nearest rupee (100 paise) using half-up integer arithmetic.
     */
    fun roundToNearestRupee(): Money = roundToRupee("NEAREST")

    /**
     * Rounds to whole rupee (100 paise) according to mode: NEAREST, UP, or DOWN.
     */
    fun roundToRupee(modeName: String): Money {
        val sign = if (paise >= 0) 1L else -1L
        val absPaise = abs(paise)
        val remainder = absPaise % 100L
        val base = (absPaise / 100L) * 100L
        if (remainder == 0L) return this
        val rounded = when (modeName.uppercase()) {
            "UP" -> base + 100L
            "DOWN" -> base
            else -> if (remainder >= 50L) base + 100L else base
        }
        return Money(sign * rounded)
    }

    override fun compareTo(other: Money): Int = this.paise.compareTo(other.paise)

    /**
     * Formats amount using Indian digit grouping (e.g., 12,34,567.00).
     */
    fun formatIndian(
        includeSymbol: Boolean = true,
        decimals: Int = 2,
        masked: Boolean = false
    ): String {
        if (masked) {
            return if (includeSymbol) "₹ ••••••" else "••••••"
        }
        val negative = paise < 0
        val absPaise = abs(paise)
        val wholeRupees = absPaise / 100L
        val remPaise = absPaise % 100L

        val groupedRupees = formatIndianWholeNumber(wholeRupees)
        val prefix = buildString {
            if (negative) append("-")
            if (includeSymbol) append("₹")
        }

        return when (decimals) {
            0 -> "$prefix$groupedRupees"
            1 -> "$prefix$groupedRupees.${(remPaise / 10L)}"
            else -> "$prefix$groupedRupees.${remPaise.toString().padStart(2, '0')}"
        }
    }

    companion object {
        val ZERO = Money(0L)

        fun fromRupees(rupees: Long): Money = Money(Math.multiplyExact(rupees, 100L))

        fun fromRupeesPaise(rupees: Long, paise: Long): Money {
            require(paise in 0..99) { "Paise part must be in 0..99" }
            val sign = if (rupees < 0) -1L else 1L
            val total = abs(rupees) * 100L + paise
            return Money(sign * total)
        }

        /**
         * Parses a string such as "12,34,567.50", "-450.05", or "1000" into integer paise without floating point.
         */
        fun parseInput(raw: String): Money {
            val cleaned = raw.trim().replace("₹", "").replace(",", "").replace(" ", "")
            if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return ZERO
            val negative = cleaned.startsWith("-")
            val unsigned = cleaned.removePrefix("-").removePrefix("+")
            val parts = unsigned.split(".")
            if (parts.size > 2) throw IllegalArgumentException("Invalid money format: $raw")

            val rupees = if (parts[0].isEmpty()) 0L else parts[0].toLong()
            val paise = if (parts.size == 2) {
                val frac = parts[1].take(2).padEnd(2, '0')
                if (frac.isEmpty()) 0L else frac.toLong()
            } else {
                0L
            }
            val combined = Math.addExact(Math.multiplyExact(rupees, 100L), paise)
            return Money(if (negative) -combined else combined)
        }

        fun formatIndianWholeNumber(number: Long): String {
            val str = abs(number).toString()
            if (str.length <= 3) return if (number < 0) "-$str" else str
            val lastThree = str.substring(str.length - 3)
            val remaining = str.substring(0, str.length - 3)
            val groups = mutableListOf<String>()
            var i = remaining.length
            while (i > 0) {
                val start = (i - 2).coerceAtLeast(0)
                groups.add(0, remaining.substring(start, i))
                i = start
            }
            val formatted = groups.joinToString(",") + "," + lastThree
            return if (number < 0) "-$formatted" else formatted
        }
    }
}
