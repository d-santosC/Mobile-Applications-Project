package com.example.mywins.database.activityEvent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityEventDao {
    @Insert
    suspend fun insertEvent(event: ActivityEvent)

    @Query("""
        SELECT * FROM activity_events 
        WHERE userEmail IN (:friendEmails)
        ORDER BY timestamp DESC
    """)
    suspend fun getEventsFromFriends(friendEmails: List<String>): List<ActivityEvent>

    @Query("DELETE FROM activity_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Int)


}