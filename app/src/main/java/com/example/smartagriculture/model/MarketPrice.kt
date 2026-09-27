package com.example.smartagriculture.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarketPrice(
    val commodity: String,
    val market: String,
    val price: String,
    val date: String
) : Parcelable
