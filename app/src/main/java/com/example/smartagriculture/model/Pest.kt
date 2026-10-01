package com.example.smartagriculture.model

import java.io.Serializable

data class Pest(
    val pestName: String,
    val affectedCrops: String,
    val solution: String
) : Serializable
