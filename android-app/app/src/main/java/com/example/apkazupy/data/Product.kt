package com.example.apkazupy.data

data class Product(
    val id: Long? = null,
    val name: String,
    val barcode: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbohydrates: Double? = null
)
