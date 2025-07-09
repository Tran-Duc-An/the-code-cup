package com.example.thecodecup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY id DESC")
    fun getOrdersByEmailFlow(email: String): Flow<List<OrderEntity>>

    //Delete all orders
    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    @Query("SELECT coffeeId, COUNT(*) as total FROM orders WHERE userEmail = :email GROUP BY coffeeId ORDER BY total DESC LIMIT 5")
    suspend fun getTopUserOrders(email: String): List<CoffeeOrderCount>

    @Query("SELECT coffeeId, COUNT(*) as total FROM orders GROUP BY coffeeId ORDER BY total DESC LIMIT 3")
    suspend fun getTopTrendingOrders(): List<CoffeeOrderCount>

    @Query("SELECT * FROM coffee WHERE id NOT IN (SELECT coffeeId FROM orders WHERE userEmail = :email)")
    suspend fun getNeverOrderedByUser(email: String): List<CoffeeEntity>

    @Query("""
    SELECT coffee.category 
    FROM `orders` 
    INNER JOIN coffee ON orders.coffeeId = coffee.id
    WHERE orders.userEmail = :email
""")
    suspend fun getUserOrderedCategories(email: String): List<String>


    @Update
    suspend fun updateOrder(order: OrderEntity)

}

data class CoffeeOrderCount(val coffeeId: Int, val total: Int)