package com.example.smartagriculture.repository

import com.example.smartagriculture.database.UserDao
import com.example.smartagriculture.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun findByEmail(email: String): User? {
        return userDao.findByEmail(email)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}