package com.example.thecodecup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val coffeeId: Int,  // NEW: Reference CoffeeEntity
    val quantity: Int,
    val shotType: String,
    val drinkType: String,
    val size: String,
    val iceLevel: Int,
    val pricePerCup: Double  // You can also fetch this dynamically from CoffeeEntity if needed
)

fun iceLevelLabel(level: Int): String {
    return when (level) {
        0 -> "Low"
        1 -> "Medium"
        2 -> "High"
        else -> "Unknown"
    }
}
