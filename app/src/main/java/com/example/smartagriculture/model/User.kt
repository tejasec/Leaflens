package com.example.smartagriculture.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String, // Salted password hash
    val passwordSalt: String? = null,
    val passwordHintQuestion: String? = null,
    val passwordHintAnswerHash: String? = null,
    val phone: String? = null,
    val language: String? = null
)