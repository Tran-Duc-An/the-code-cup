package com.example.thecodecup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee")
data class CoffeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val imageRes: Int,
    val description: String,
    val price: Double,
    val redeemPoint: Int,
    val category: String  // NEW FIELD (e.g., "Milk-based", "Black Coffee", "Chocolate", etc.)
)
