package com.example.mywins.database.friendships

import androidx.room.Entity

@Entity(tableName = "friendships", primaryKeys = ["userEmail", "friendEmail"])
data class Friendship(
    val userEmail: String,
    val friendEmail: String
)