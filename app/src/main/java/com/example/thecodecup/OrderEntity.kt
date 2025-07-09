package com.example.thecodecup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val coffeeId: Int,
    val address: String,
    val price: Double,
    val quantity: Int,
    val dateTime: String,
    val status: String = "ongoing" // Default = ongoing
)
