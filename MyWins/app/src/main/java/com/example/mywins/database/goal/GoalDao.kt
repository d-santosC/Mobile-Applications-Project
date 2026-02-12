package com.example.mywins.database.goal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Query("SELECT * FROM goals")
    suspend fun getAll(): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Int): Goal?

    @Query("SELECT * FROM goals WHERE userEmail = :email")
    suspend fun getAllForUser(email: String): List<Goal>

    @Query("""
    SELECT * FROM goals
    WHERE userEmail = :email
    AND done = 1
    AND doneDate IS NOT NULL
    """)
    suspend fun getDoneGoalsForUser(email: String): List<Goal>

    @Query("""
    SELECT * FROM goals
    WHERE userEmail = :email
    AND (done = 0 OR doneDate IS NULL)
    """)
    suspend fun getPendingGoalsForUser(email: String): List<Goal>

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)
}