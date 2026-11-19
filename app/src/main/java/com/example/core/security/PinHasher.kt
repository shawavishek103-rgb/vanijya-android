package com.example.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Generates salted cryptographic hashes for 6-digit app PINs and user switch PINs.
 */
object PinHasher {

    private const val ITERATIONS = 12_000
    private const val KEY_LENGTH_BITS = 256

    data class SaltedPin(val hashHex: String, val saltHex: String)

    fun hashPin(pin: String, saltHex: String = generateSaltHex()): SaltedPin {
        val saltBytes = hexToBytes(saltHex)
        val hashBytes = try {
            val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } catch (_: Exception) {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(saltBytes)
            digest.digest(pin.toByteArray(Charsets.UTF_8))
        }
        return SaltedPin(hashHex = bytesToHex(hashBytes), saltHex = saltHex)
    }

    fun verifyPin(inputPin: String, expectedHashHex: String, saltHex: String): Boolean {
        if (inputPin.length != 6 || expectedHashHex.isBlank() || saltHex.isBlank()) return false
        val computed = hashPin(inputPin, saltHex)
        return MessageDigest.isEqual(
            computed.hashHex.toByteArray(Charsets.UTF_8),
            expectedHashHex.toByteArray(Charsets.UTF_8)
        )
    }

    fun generateSaltHex(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytesToHex(bytes)
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = if (hex.length % 2 == 0) hex else "0$hex"
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
