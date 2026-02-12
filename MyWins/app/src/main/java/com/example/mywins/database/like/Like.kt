package com.example.mywins.database.like

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "likes")
data class Like(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val userEmail: String
)