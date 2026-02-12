package com.example.mywins.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Streak(
    @PrimaryKey val goalId: Int,
    val daysCompleted: Int
)