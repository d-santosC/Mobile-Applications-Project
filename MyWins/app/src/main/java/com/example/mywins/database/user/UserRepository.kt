package com.example.mywins.database.user

import android.content.Context
import com.example.mywins.database.AppDatabase

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao()

    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUser(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}