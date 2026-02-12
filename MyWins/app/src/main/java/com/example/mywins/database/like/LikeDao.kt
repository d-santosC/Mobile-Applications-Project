package com.example.mywins.database.like

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LikeDao {
    @Insert
    suspend fun addLike(like: Like)

    @Query("SELECT COUNT(*) FROM likes WHERE eventId = :eventId")
    suspend fun getLikeCount(eventId: Int): Int

    @Query("SELECT * FROM likes WHERE eventId = :eventId AND userEmail = :userEmail")
    suspend fun hasUserLiked(eventId: Int, userEmail: String): Like?

    @Query("DELETE FROM likes WHERE eventId = :eventId AND userEmail = :userEmail")
    suspend fun removeLike(eventId: Int, userEmail: String)

    @Query("DELETE FROM likes WHERE eventId = :eventId")
    suspend fun deleteLikesByEventId(eventId: Int)

}