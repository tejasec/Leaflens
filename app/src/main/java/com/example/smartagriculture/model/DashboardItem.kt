package com.example.smartagriculture.model

data class DashboardItem(
    val title: String,
    val iconRes: Int,
    val navActionId: Int,
    val accentColorHex: String = "#10B981"
)
