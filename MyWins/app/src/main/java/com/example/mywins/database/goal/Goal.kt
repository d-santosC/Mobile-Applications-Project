package com.example.mywins.database.goal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val frequency: String,
    val isPublic: Boolean,
    val done: Boolean = false,
    val createdDate: String = "",
    val userEmail: String = "",
    val doneDate: String? = null,
    val customDate: String? = null
) : Serializable