package com.example.thecodecup

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, CartEntity::class, OrderEntity::class, CoffeeEntity::class, RedeemEntity::class],
    version = 12
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun coffeeDao(): CoffeeDao
    abstract fun redeemDao(): RedeemDao
}

