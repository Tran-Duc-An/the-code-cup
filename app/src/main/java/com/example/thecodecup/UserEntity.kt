package com.example.thecodecup

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val address: String,
    val loyaltyStamps: Int = 0,
    val loyaltyPoint: Int = 0
)
