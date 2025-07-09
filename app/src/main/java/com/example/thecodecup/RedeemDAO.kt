package com.example.thecodecup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RedeemDao {

    @Insert
    suspend fun insertRedeem(redeem: RedeemEntity)

    @Query("SELECT * FROM redeem")
    fun getAllRedeemItems(): Flow<List<RedeemEntity>>

    //Delete all redeem items
    @Query("DELETE FROM redeem")
    suspend fun deleteAllRedeemItems()

}
