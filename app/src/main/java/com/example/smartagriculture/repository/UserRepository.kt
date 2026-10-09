package com.example.smartagriculture.repository

import android.content.Context
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.UserDao
import com.example.smartagriculture.model.User
import com.example.smartagriculture.utils.SecurityUtils
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        private var failedAttemptCount: Int = 0
        private var lockoutUntilTimestamp: Long = 0L
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L

        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val instance = UserRepository(db.userDao())
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Checks if login attempts are currently rate-limited.
     */
    fun isLockedOut(): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        if (now < lockoutUntilTimestamp) {
            val remainingSec = (lockoutUntilTimestamp - now) / 1000
            return Pair(true, remainingSec)
        }
        return Pair(false, 0)
    }

    fun recordFailedAttempt() {
        failedAttemptCount++
        if (failedAttemptCount >= MAX_FAILED_ATTEMPTS) {
            lockoutUntilTimestamp = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            failedAttemptCount = 0
        }
    }

    fun resetFailedAttempts() {
        failedAttemptCount = 0
        lockoutUntilTimestamp = 0L
    }

    /**
     * Registers a new user with salted password and salted hint answer.
     */
    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        hintQuestion: String,
        hintAnswer: String
    ): Result<Long> {
        // Enforce password policy
        val (isValidPass, passError) = SecurityUtils.validatePasswordPolicy(password)
        if (!isValidPass) {
            return Result.failure(IllegalArgumentException(passError ?: "Invalid password."))
        }

        // Enforce hint security: hint cannot contain or equal password
        if (!SecurityUtils.validateHintDoesNotExposePassword(hintQuestion, password)) {
            return Result.failure(IllegalArgumentException("Security hint must not contain or match your password."))
        }
        if (!SecurityUtils.validateHintDoesNotExposePassword(hintAnswer, password)) {
            return Result.failure(IllegalArgumentException("Hint answer must not contain or match your password."))
        }

        val existing = userDao.findByEmail(email)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val hintAnswerHash = SecurityUtils.hashHintAnswer(hintAnswer, salt)

        val newUser = User(
            name = name,
            email = email,
            password = passwordHash,
            passwordSalt = salt,
            passwordHintQuestion = hintQuestion.trim(),
            passwordHintAnswerHash = hintAnswerHash
        )

        val id = userDao.insertUser(newUser)
        return Result.success(id)
    }

    /**
     * Validates credentials locally against stored salted hash.
     */
    suspend fun login(email: String, password: String): Result<User> {
        val (isLocked, remainingSec) = isLockedOut()
        if (isLocked) {
            return Result.failure(IllegalStateException("Too many failed attempts. Locked out for $remainingSec seconds."))
        }

        val user = userDao.findByEmail(email)
        if (user == null || user.passwordSalt == null) {
            recordFailedAttempt()
            return Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        val matches = SecurityUtils.verifyPassword(password, user.passwordSalt, user.password)
        if (!matches) {
            recordFailedAttempt()
            return Result.failure(IllegalArgumentException("Invalid email or password."))
        }

        resetFailedAttempts()
        return Result.success(user)
    }

    /**
     * Retrieves the stored security hint question for a given user email.
     */
    suspend fun getHintQuestion(email: String): String? {
        val user = userDao.findByEmail(email) ?: return null
        return user.passwordHintQuestion
    }

    /**
     * Resets a user's password using hint answer verification (NO email/SMS/OTP).
     */
    suspend fun resetPasswordWithHint(
        email: String,
        hintAnswer: String,
        newPassword: String
    ): Result<Unit> {
        val (isValidPass, passError) = SecurityUtils.validatePasswordPolicy(newPassword)
        if (!isValidPass) {
            return Result.failure(IllegalArgumentException(passError ?: "Invalid password."))
        }

        val user = userDao.findByEmail(email)
            ?: return Result.failure(IllegalArgumentException("No account found or no hint set."))

        val salt = user.passwordSalt
            ?: return Result.failure(IllegalStateException("Security credentials missing for account."))

        val expectedAnswerHash = user.passwordHintAnswerHash
            ?: return Result.failure(IllegalStateException("No hint answer set for this account."))

        val isAnswerCorrect = SecurityUtils.verifyHintAnswer(hintAnswer, salt, expectedAnswerHash)
        if (!isAnswerCorrect) {
            return Result.failure(IllegalArgumentException("Incorrect answer to security hint."))
        }

        // Verify that hint does not contain new password
        val question = user.passwordHintQuestion ?: ""
        if (!SecurityUtils.validateHintDoesNotExposePassword(question, newPassword) ||
            !SecurityUtils.validateHintDoesNotExposePassword(hintAnswer, newPassword)
        ) {
            return Result.failure(IllegalArgumentException("New password cannot be contained in the security hint."))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newPasswordHash = SecurityUtils.hashPassword(newPassword, newSalt)
        val newAnswerHash = SecurityUtils.hashHintAnswer(hintAnswer, newSalt)

        val updatedUser = user.copy(
            password = newPasswordHash,
            passwordSalt = newSalt,
            passwordHintAnswerHash = newAnswerHash
        )

        userDao.updateUser(updatedUser)
        return Result.success(Unit)
    }

    suspend fun findByEmail(email: String): User? {
        return userDao.findByEmail(email)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}