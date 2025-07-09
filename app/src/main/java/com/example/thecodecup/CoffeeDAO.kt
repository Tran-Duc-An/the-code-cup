package com.example.thecodecup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoffee(coffee: CoffeeEntity)

    @Query("SELECT * FROM coffee")
    fun getAllCoffees(): Flow<List<CoffeeEntity>>

    @Query("SELECT * FROM coffee WHERE id = :coffeeId LIMIT 1")
    suspend fun getCoffeeById(coffeeId: Int): CoffeeEntity?

    @Query("DELETE FROM coffee")
    suspend fun deleteAllCoffees()

    @Query("""
    SELECT * FROM coffee 
    WHERE category IN (:categories) 
    AND id NOT IN (
        SELECT coffeeId FROM `orders` WHERE userEmail = :email
    )
""")
    suspend fun getRecommendedCoffees(email: String, categories: List<String>): List<CoffeeEntity>

    @Query("SELECT * FROM coffee ORDER BY RANDOM() LIMIT 5")
    suspend fun getRandomCoffees(): List<CoffeeEntity>

}
