package com.example.mywins.database.friendships

import androidx.room.Entity

@Entity(tableName = "friend_requests", primaryKeys = ["fromEmail", "toEmail"])
data class FriendRequest(
    val fromEmail: String,
    val toEmail: String,
    val timestamp: Long
)