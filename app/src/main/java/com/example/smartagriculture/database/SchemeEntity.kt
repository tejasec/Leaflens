package com.example.smartagriculture.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "cached_schemes")
data class SchemeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val eligibility: String,
    val details: String,
    val applicationUrl: String,
    val subsidyAmount: String = "",
    val cachedTimestamp: Long = System.currentTimeMillis()
) : Serializable
