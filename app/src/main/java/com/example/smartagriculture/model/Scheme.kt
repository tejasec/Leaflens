package com.example.smartagriculture.model

import java.io.Serializable

data class Scheme(
    val title: String,
    val description: String,
    val eligibility: String,
    val fullDetails: String = "",
    val link: String = ""
) : Serializable
