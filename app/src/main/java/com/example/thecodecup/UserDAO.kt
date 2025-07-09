package com.example.thecodecup

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET loyaltyStamps = :stamps, loyaltyPoint = :points WHERE email = :email")
    suspend fun updateStampsAndPoints(email: String, stamps: Int, points: Int)

    @Query("UPDATE users SET loyaltyPoint = loyaltyPoint - :points WHERE email = :email")
    suspend fun decreaseLoyaltyPoints(email: String, points: Int)

    //Delete all users
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
