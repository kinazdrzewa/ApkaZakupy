package com.example.apkazupy.data

data class Suggestion(
    val id: Long? = null,
    val userId: Long? = null,
    val productName: String? = null,
    val barcode: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbohydrates: Double? = null,
    val comment: String? = null,
    val createdAt: Any? = null
)
