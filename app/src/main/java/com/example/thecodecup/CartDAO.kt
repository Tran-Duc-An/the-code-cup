package com.example.thecodecup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Query("SELECT * FROM cart WHERE userEmail = :email")
    fun getCartItemsByEmailFlow(email: String): Flow<List<CartEntity>>

    @Query("DELETE FROM cart WHERE id = :cartItemId")
    suspend fun deleteCartItemById(cartItemId: Int)

    @Query("DELETE FROM cart WHERE userEmail = :email")
    suspend fun clearCartByEmail(email: String)

    // Correct SQL for finding matching item
    @Query("""
        SELECT * FROM cart 
        WHERE userEmail = :email 
          AND coffeeId = :coffeeId 
          AND shotType = :shotType 
          AND drinkType = :drinkType 
          AND size = :size 
          AND iceLevel = :iceLevel 
        LIMIT 1
    """)
    suspend fun findMatchingItem(
        email: String,
        coffeeId: Int,
        shotType: String,
        drinkType: String,
        size: String,
        iceLevel: Int
    ): CartEntity?

    @Query("UPDATE cart SET quantity = quantity + :amount WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Int, amount: Int)

    //Delete all cart
    @Query("DELETE FROM cart")
    suspend fun deleteAllCartItems()
}
