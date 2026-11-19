package com.example

import com.example.core.model.IndianNumberToWords
import com.example.core.model.Money
import com.example.core.model.Validators
import com.example.core.security.PinHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun money_additionSubtractionAndMultiplication_areExactInPaise() {
        val m1 = Money.fromRupeesPaise(12_34_567L, 50L)
        val m2 = Money.fromRupeesPaise(432L, 75L)
        val sum = m1 + m2
        assertEquals(123500025L, sum.paise)
        assertEquals("₹12,35,000.25", sum.formatIndian())

        val diff = m1 - m2
        assertEquals(123413475L, diff.paise)
        assertEquals("₹12,34,134.75", diff.formatIndian())
    }

    @Test
    fun money_indianGrouping_formatsLakhsAndCroresCorrectly() {
        assertEquals("₹0.00", Money.ZERO.formatIndian())
        assertEquals("₹999.00", Money.fromRupees(999L).formatIndian())
        assertEquals("₹1,000.00", Money.fromRupees(1_000L).formatIndian())
        assertEquals("₹12,34,567.00", Money.fromRupees(12_34_567L).formatIndian())
        assertEquals("₹9,87,65,432.50", Money.fromRupeesPaise(9_87_65_432L, 50L).formatIndian())
        assertEquals("-₹1,25,000.00", (-Money.fromRupees(1_25_000L)).formatIndian())
        assertEquals("₹ ••••••", Money.fromRupees(50_000L).formatIndian(masked = true))
    }

    @Test
    fun money_percentageBasisPointsAndRounding_useHalfUpIntegerMath() {
        val taxable = Money.fromRupees(1_000L) // 1000.00
        val gst18 = taxable.percentageBasisPoints(1800) // 18%
        assertEquals(Money.fromRupees(180L), gst18)

        val diamondTax = Money.fromRupees(10_000L).percentageBasisPoints(25) // 0.25%
        assertEquals(Money.fromRupees(25L), diamondTax)

        val unrounded = Money.fromRupeesPaise(149L, 50L)
        assertEquals(Money.fromRupees(150L), unrounded.roundToNearestRupee())

        val roundDown = Money.fromRupeesPaise(149L, 49L)
        assertEquals(Money.fromRupees(149L), roundDown.roundToNearestRupee())
    }

    @Test
    fun money_parseInput_parsesIndianFormattedStringsWithoutFloats() {
        val parsed = Money.parseInput("₹12,34,567.85")
        assertEquals(123456785L, parsed.paise)
        assertEquals(Money.ZERO, Money.parseInput(""))
    }

    @Test
    fun indianNumberToWords_convertsLakhsCroresAndPaiseCorrectly() {
        assertEquals(
            "Rupees Zero Only",
            IndianNumberToWords.toCurrencyWords(Money.ZERO)
        )
        assertEquals(
            "Rupees One Lakh Twenty-Three Thousand Four Hundred Fifty-Six Only",
            IndianNumberToWords.toCurrencyWords(Money.fromRupees(1_23_456L))
        )
        assertEquals(
            "Rupees Twelve Lakh Thirty-Four Thousand Five Hundred Sixty-Seven and Paise Fifty Only",
            IndianNumberToWords.toCurrencyWords(Money.fromRupeesPaise(12_34_567L, 50L))
        )
        assertEquals(
            "Rupees Five Crore Five Lakh Five Thousand Fifty Only",
            IndianNumberToWords.toCurrencyWords(Money.fromRupees(5_05_05_050L))
        )
        assertEquals(
            "Paise Seventy-Five Only",
            IndianNumberToWords.toCurrencyWords(Money(75L))
        )
    }

    @Test
    fun validators_validateGstinAndExtractPanAndStateOffline() {
        val base14 = "27AAPFU0939F1Z"
        val checkChar = Validators.computeGstinCheckDigit(base14)
        val validGstin = base14 + checkChar

        assertEquals(
            Validators.ValidationResult.Valid,
            Validators.validateGstin(validGstin, verifyChecksumStrictly = true)
        )
        assertEquals("AAPFU0939F", Validators.extractPanFromGstin(validGstin))
        assertEquals("Maharashtra", Validators.extractStateFromGstin(validGstin)?.name)

        val badState = Validators.validateGstin("99AAPFU0939F1ZV")
        assertTrue(badState is Validators.ValidationResult.Invalid)
    }

    @Test
    fun pinHasher_hashesAndVerifiesSixDigitPin() {
        val salted = PinHasher.hashPin("123456")
        assertTrue(PinHasher.verifyPin("123456", salted.hashHex, salted.saltHex))
        assertFalse(PinHasher.verifyPin("654321", salted.hashHex, salted.saltHex))
    }

    @Test
    fun money_roundToRupee_supportsNearestUpAndDownModes() {
        val amount = Money.fromRupeesPaise(149L, 20L)
        assertEquals(Money.fromRupees(149L), amount.roundToRupee("NEAREST"))
        assertEquals(Money.fromRupees(150L), amount.roundToRupee("UP"))
        assertEquals(Money.fromRupees(149L), amount.roundToRupee("DOWN"))

        val highFraction = Money.fromRupeesPaise(149L, 80L)
        assertEquals(Money.fromRupees(150L), highFraction.roundToRupee("NEAREST"))
        assertEquals(Money.fromRupees(150L), highFraction.roundToRupee("UP"))
        assertEquals(Money.fromRupees(149L), highFraction.roundToRupee("DOWN"))
    }
}
