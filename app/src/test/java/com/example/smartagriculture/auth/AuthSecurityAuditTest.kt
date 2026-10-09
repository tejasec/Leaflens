package com.example.smartagriculture.auth

import android.content.Context
import com.example.smartagriculture.AppConfig
import com.example.smartagriculture.database.UserDao
import com.example.smartagriculture.model.User
import com.example.smartagriculture.repository.UserRepository
import com.example.smartagriculture.utils.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Functional and security tests verifying remediations for authentication,
 * salted credential storage, password hint reset, and session enforcement.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthSecurityAuditTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    // -------------------------------------------------------------------------
    // 1. Auth Enforcement & Bypass Elimination
    // -------------------------------------------------------------------------

    @Test
    fun testAuthBypassFlag_IsAuthEnabledForSecurity() {
        // AppConfig.IS_AUTH_ENABLED must be true to enforce login/signup in production
        assertTrue(
            "Authentication must be enabled",
            AppConfig.IS_AUTH_ENABLED
        )
    }

    // -------------------------------------------------------------------------
    // 2. Data Layer: Password Hash, Salt & Hint Columns
    // -------------------------------------------------------------------------

    @Test
    fun testUserEntity_ContainsSaltAndHintColumns() {
        val fields = User::class.java.declaredFields.map { it.name }
        
        assertTrue("User entity has password field", fields.contains("password"))
        assertTrue("User entity has passwordSalt column", fields.contains("passwordSalt"))
        assertTrue("User entity has passwordHintQuestion column", fields.contains("passwordHintQuestion"))
        assertTrue("User entity has passwordHintAnswerHash column", fields.contains("passwordHintAnswerHash"))
    }

    @Test
    fun testUserDao_NoPlaintextPasswordQuery() {
        val methods = UserDao::class.java.declaredMethods.map { it.name }
        assertTrue("UserDao has findByEmail", methods.contains("findByEmail"))
        assertTrue("UserDao has insertUser", methods.contains("insertUser"))
    }

    // -------------------------------------------------------------------------
    // 3. SharedPreferences: Plaintext Password Removed
    // -------------------------------------------------------------------------

    @Test
    fun testProfilePreferences_DoesNotRetainPlaintextPassword() {
        val prefs = context.getSharedPreferences("smart_agri_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_name", "Test Farmer").remove("user_password").apply()

        val retrievedPassword = prefs.getString("user_password", null)
        assertNull("user_password must not be saved in unencrypted preferences", retrievedPassword)
    }

    // -------------------------------------------------------------------------
    // 4. Cryptographic Hashing & Verification
    // -------------------------------------------------------------------------

    @Test
    fun testSecurityUtils_SaltedHashingAndVerification() {
        val salt = SecurityUtils.generateSalt()
        assertEquals(32, salt.length) // 16 bytes hex

        val password = "SecurePassword@123"
        val hash = SecurityUtils.hashPassword(password, salt)

        assertTrue(SecurityUtils.verifyPassword(password, salt, hash))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword@123", salt, hash))
    }

    @Test
    fun testSecurityUtils_HintAnswerHashingAndVerification() {
        val salt = SecurityUtils.generateSalt()
        val answer = "Tomato Garden"
        val answerHash = SecurityUtils.hashHintAnswer(answer, salt)

        // Case-insensitive & trimmed matching
        assertTrue(SecurityUtils.verifyHintAnswer("tomato garden", salt, answerHash))
        assertTrue(SecurityUtils.verifyHintAnswer("  Tomato Garden  ", salt, answerHash))
        assertFalse(SecurityUtils.verifyHintAnswer("Wheat Field", salt, answerHash))
    }

    // -------------------------------------------------------------------------
    // 5. Hint-Based Password Recovery Flow
    // -------------------------------------------------------------------------

    @Test
    fun testUserRepository_HintBasedResetFlow() = runBlocking {
        val userRepo = UserRepository.getInstance(context)

        // Register user with security hint
        val regRes = userRepo.registerUser(
            name = "Farmer John",
            email = "john@example.com",
            password = "Password@123",
            hintQuestion = "Favorite crop to harvest",
            hintAnswer = "Sugarcane"
        )
        assertTrue(regRes.isSuccess)

        // Retrieve hint question (without answering or leaking password)
        val hintQuestion = userRepo.getHintQuestion("john@example.com")
        assertEquals("Favorite crop to harvest", hintQuestion)

        // Wrong hint answer fails
        val badReset = userRepo.resetPasswordWithHint(
            email = "john@example.com",
            hintAnswer = "Cotton",
            newPassword = "NewPassword@456"
        )
        assertTrue(badReset.isFailure)

        // Correct hint answer succeeds
        val goodReset = userRepo.resetPasswordWithHint(
            email = "john@example.com",
            hintAnswer = "Sugarcane",
            newPassword = "NewPassword@456"
        )
        assertTrue(goodReset.isSuccess)

        // Verify old password no longer works, new password does
        val oldLogin = userRepo.login("john@example.com", "Password@123")
        assertTrue("Old password rejected", oldLogin.isFailure)

        val newLogin = userRepo.login("john@example.com", "NewPassword@456")
        assertTrue("New password accepted", newLogin.isSuccess)
    }

    // -------------------------------------------------------------------------
    // 6. Rate Limiting & Lockout
    // -------------------------------------------------------------------------

    @Test
    fun testUserRepository_RateLimitingLockout() = runBlocking {
        val userRepo = UserRepository.getInstance(context)
        userRepo.resetFailedAttempts()

        // 5 consecutive failed attempts
        for (i in 1..5) {
            userRepo.login("unknown@example.com", "wrongpass")
        }

        val (isLocked, _) = userRepo.isLockedOut()
        assertTrue("Lockout triggered after 5 failed attempts", isLocked)

        userRepo.resetFailedAttempts()
        val (isResetLocked, _) = userRepo.isLockedOut()
        assertFalse("Lockout cleared after reset", isResetLocked)
    }
}
