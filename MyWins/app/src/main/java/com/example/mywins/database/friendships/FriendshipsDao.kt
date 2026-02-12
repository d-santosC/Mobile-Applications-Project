package com.example.mywins.database.friendships

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FriendshipsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFriendship(friendship: Friendship)

    @Query("SELECT friendEmail FROM friendships WHERE userEmail = :email")
    suspend fun getFriendsOfUser(email: String): List<String>

    @Query("DELETE FROM friendships WHERE userEmail = :user AND friendEmail = :friend")
    suspend fun removeFriendship(user: String, friend: String)
}