package com.example.mywins.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mywins.database.activityEvent.ActivityEventDao
import com.example.mywins.database.activityEvent.ActivityEvent
import com.example.mywins.database.comment.Comment
import com.example.mywins.database.comment.CommentDao
import com.example.mywins.database.friendships.FriendRequest
import com.example.mywins.database.friendships.FriendRequestDao
import com.example.mywins.database.friendships.Friendship
import com.example.mywins.database.friendships.FriendshipsDao
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalDao
import com.example.mywins.database.like.Like
import com.example.mywins.database.like.LikeDao
import com.example.mywins.database.user.User
import com.example.mywins.database.user.UserDao
import com.example.mywins.utils.Converters

@Database(entities = [
    User::class, Goal::class,
    Friendship::class,
    FriendRequest::class,
    ActivityEvent::class,
    Like::class,
    Comment::class],
    version = 15)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
    abstract fun activityEventDao(): ActivityEventDao
    abstract fun likeDao(): LikeDao
    abstract fun commentDao(): CommentDao
    abstract fun friendshipsDao(): FriendshipsDao
    abstract fun friendRequestDao(): FriendRequestDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mywins-db"

                )
                //.fallbackToDestructiveMigration()
                .build().also { instance = it }
            }
        }
    }
}