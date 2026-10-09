package com.example.smartagriculture.utils

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Security and cryptographic utilities for local credential management,
 * password salting/hashing, and hint-based authentication verification.
 */
object SecurityUtils {

    private val secureRandom = SecureRandom()

    /**
     * Generates a 16-byte random hex salt string.
     */
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hashes a password combined with a cryptographic salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String): String {
        val input = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Normalizes and hashes a hint answer with salt.
     */
    fun hashHintAnswer(answer: String, salt: String): String {
        val normalized = answer.trim().lowercase()
        return hashPassword(normalized, salt)
    }

    /**
     * Verifies a candidate password against the stored salt and hash.
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computed = hashPassword(password, salt)
        return MessageDigest.isEqual(computed.toByteArray(), expectedHash.toByteArray())
    }

    /**
     * Verifies candidate hint answer against stored answer hash.
     */
    fun verifyHintAnswer(answer: String, salt: String, expectedHash: String): Boolean {
        val computed = hashHintAnswer(answer, salt)
        return MessageDigest.isEqual(computed.toByteArray(), expectedHash.toByteArray())
    }

    /**
     * Validates that the password hint question or text does NOT contain or equal the password.
     */
    fun validateHintDoesNotExposePassword(hint: String, password: String): Boolean {
        val trimmedHint = hint.trim()
        val trimmedPassword = password.trim()
        if (trimmedHint.isEmpty() || trimmedPassword.isEmpty()) return true
        if (trimmedHint.equals(trimmedPassword, ignoreCase = true)) return false
        if (trimmedHint.contains(trimmedPassword, ignoreCase = true)) return false
        return true
    }

    /**
     * Validates password policy: minimum 8 characters, at least 1 digit or symbol.
     */
    fun validatePasswordPolicy(password: String): Pair<Boolean, String?> {
        if (password.length < 8) {
            return Pair(false, "Password must be at least 8 characters long.")
        }
        val hasDigitOrSymbol = password.any { it.isDigit() || !it.isLetterOrDigit() }
        if (!hasDigitOrSymbol) {
            return Pair(false, "Password must contain at least one number or special character.")
        }
        return Pair(true, null)
    }
}
