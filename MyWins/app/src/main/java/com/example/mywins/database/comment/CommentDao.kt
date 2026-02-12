package com.example.mywins.database.comment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CommentDao {
    @Insert
    suspend fun addComment(comment: Comment)

    @Query("SELECT * FROM comments WHERE eventId = :eventId ORDER BY timestamp")
    suspend fun getComments(eventId: Int): List<Comment>

    @Query("DELETE FROM comments WHERE eventId = :eventId")
    suspend fun deleteCommentsByEventId(eventId: Int)
}