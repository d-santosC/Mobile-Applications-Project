package com.example.mywins.database.activityEvent

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_events")
data class ActivityEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val type: String, //"created", "completed"
    val goalName: String,
    val timestamp: Long
)