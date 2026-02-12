package com.example.mywins.database.comment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val userEmail: String,
    val text: String,
    val timestamp: Long
)