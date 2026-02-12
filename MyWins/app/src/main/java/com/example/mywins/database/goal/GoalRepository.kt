package com.example.mywins.database.goal

import android.content.Context
import com.example.mywins.database.AppDatabase
import com.example.mywins.database.activityEvent.ActivityEvent

class GoalRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.goalDao()

    suspend fun insertGoal(goal: Goal) = dao.insert(goal)
    suspend fun getAllGoals() = dao.getAll()
    suspend fun getGoalById(id: Int) = dao.getById(id)
    suspend fun updateGoal(goal: Goal) = dao.update(goal)
    suspend fun getAllGoalsForUser(email: String) = dao.getAllForUser(email)
    suspend fun getDoneGoalsForUser(email: String) = dao.getDoneGoalsForUser(email)
    suspend fun getPendingGoalsForUser(email: String) = dao.getPendingGoalsForUser(email)
    suspend fun deleteGoal(goal: Goal) = dao.delete(goal)
    suspend fun insertActivityEventIfPublic(goal: Goal, type: String) {
        if (goal.isPublic) {
            val eventDao = db.activityEventDao()
            val event = ActivityEvent(
                userEmail = goal.userEmail,
                type = type,
                goalName = goal.name,
                timestamp = System.currentTimeMillis()
            )
            eventDao.insertEvent(event)
        }
    }
}