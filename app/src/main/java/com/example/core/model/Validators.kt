package com.example.core.model

/**
 * Offline validation for Indian GSTIN (15-char format + Mod-36 checksum), PAN, and Indian State Codes.
 */
object Validators {

    private const val CHECKSUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val GSTIN_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
    private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
    private val UPI_REGEX = Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")

    data class IndianState(val code: String, val name: String)

    val indianStates: List<IndianState> = listOf(
        IndianState("01", "Jammu & Kashmir"),
        IndianState("02", "Himachal Pradesh"),
        IndianState("03", "Punjab"),
        IndianState("04", "Chandigarh"),
        IndianState("05", "Uttarakhand"),
        IndianState("06", "Haryana"),
        IndianState("07", "Delhi"),
        IndianState("08", "Rajasthan"),
        IndianState("09", "Uttar Pradesh"),
        IndianState("10", "Bihar"),
        IndianState("11", "Sikkim"),
        IndianState("12", "Arunachal Pradesh"),
        IndianState("13", "Nagaland"),
        IndianState("14", "Manipur"),
        IndianState("15", "Mizoram"),
        IndianState("16", "Tripura"),
        IndianState("17", "Meghalaya"),
        IndianState("18", "Assam"),
        IndianState("19", "West Bengal"),
        IndianState("20", "Jharkhand"),
        IndianState("21", "Odisha"),
        IndianState("22", "Chhattisgarh"),
        IndianState("23", "Madhya Pradesh"),
        IndianState("24", "Gujarat"),
        IndianState("26", "Dadra & Nagar Haveli and Daman & Diu"),
        IndianState("27", "Maharashtra"),
        IndianState("29", "Karnataka"),
        IndianState("30", "Goa"),
        IndianState("31", "Lakshadweep"),
        IndianState("32", "Kerala"),
        IndianState("33", "Tamil Nadu"),
        IndianState("34", "Puducherry"),
        IndianState("35", "Andaman & Nicobar Islands"),
        IndianState("36", "Telangana"),
        IndianState("37", "Andhra Pradesh"),
        IndianState("38", "Ladakh")
    )

    val indianStateMap: Map<String, String> = indianStates.associate { it.code to it.name }

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    /**
     * Validates a 15-character Indian GSTIN offline:
     * 1. Checks length & structure (2-digit state code + 10-char PAN + entity code + 'Z' + check digit).
     * 2. Verifies valid Indian state code.
     * 3. Verifies Mod-36 checksum character (or accepts structurally valid GSTINs with checksum notice).
     */
    fun validateGstin(raw: String, verifyChecksumStrictly: Boolean = false): ValidationResult {
        val gstin = raw.trim().uppercase()
        if (gstin.isEmpty()) return ValidationResult.Valid // Optional when non-GST
        if (gstin.length != 15) {
            return ValidationResult.Invalid("GSTIN must be exactly 15 characters (${gstin.length}/15)")
        }
        val stateCode = gstin.substring(0, 2)
        if (indianStates.none { it.code == stateCode }) {
            return ValidationResult.Invalid("Invalid 2-digit GST state code: $stateCode")
        }
        if (!GSTIN_REGEX.matches(gstin)) {
            return ValidationResult.Invalid("Invalid format. Expected: 27AAPFU0939F1ZV")
        }
        if (verifyChecksumStrictly) {
            val expectedCheck = computeGstinCheckDigit(gstin.substring(0, 14))
            if (gstin[14] != expectedCheck) {
                return ValidationResult.Invalid("GSTIN checksum mismatch (expected '$expectedCheck' at position 15)")
            }
        }
        return ValidationResult.Valid
    }

    fun computeGstinCheckDigit(first14: String): Char {
        var factor = 1
        var sum = 0
        val mod = CHECKSUM_CHARS.length
        for (i in 0 until 14) {
            val codePoint = CHECKSUM_CHARS.indexOf(first14[i])
            val digit = factor * codePoint
            factor = if (factor == 2) 1 else 2
            sum += (digit / mod) + (digit % mod)
        }
        val checkCodePoint = (mod - (sum % mod)) % mod
        return CHECKSUM_CHARS[checkCodePoint]
    }

    fun extractPanFromGstin(gstin: String): String? {
        val upper = gstin.trim().uppercase()
        return if (upper.length >= 12 && PAN_REGEX.matches(upper.substring(2, 12))) {
            upper.substring(2, 12)
        } else null
    }

    fun extractStateFromGstin(gstin: String): IndianState? {
        val upper = gstin.trim().uppercase()
        if (upper.length < 2) return null
        val code = upper.substring(0, 2)
        return indianStates.find { it.code == code }
    }

    fun validatePan(raw: String): ValidationResult {
        val pan = raw.trim().uppercase()
        if (pan.isEmpty()) return ValidationResult.Valid
        if (pan.length != 10 || !PAN_REGEX.matches(pan)) {
            return ValidationResult.Invalid("PAN must be 10 characters (e.g., ABCDE1234F)")
        }
        return ValidationResult.Valid
    }

    fun validateUpiId(raw: String): ValidationResult {
        val upi = raw.trim()
        if (upi.isEmpty()) return ValidationResult.Valid
        return if (UPI_REGEX.matches(upi)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Invalid UPI ID (e.g., merchant@okaxis)")
        }
    }
}
