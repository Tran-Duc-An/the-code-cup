package com.example.thecodecup

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "redeem",
    foreignKeys = [
        ForeignKey(
            entity = CoffeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["coffeeId"],
            onDelete = ForeignKey.CASCADE // Optional: delete redeem item if coffee deleted
        )
    ],
    indices = [Index(value = ["coffeeId"])] // Improves lookup performance
)
data class RedeemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coffeeId: Int,    // Must exist in CoffeeEntity
    val redeemPoint: Int,
    val availableUntil: String? = null
)
