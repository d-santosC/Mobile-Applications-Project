package com.example.mywins.database.friendships

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FriendRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendRequest(request: FriendRequest)

    @Query("SELECT * FROM friend_requests WHERE toEmail = :userEmail")
    suspend fun getReceivedRequests(userEmail: String): List<FriendRequest>

    @Query("DELETE FROM friend_requests WHERE fromEmail = :from AND toEmail = :to")
    suspend fun deleteRequest(from: String, to: String)
}