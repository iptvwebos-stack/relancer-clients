package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientSubscriptionDao {
    @Query("SELECT * FROM client_subscriptions ORDER BY id ASC")
    fun getAllSubscriptions(): Flow<List<ClientSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: ClientSubscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<ClientSubscription>)

    @Update
    suspend fun updateSubscription(subscription: ClientSubscription)

    @Delete
    suspend fun deleteSubscription(subscription: ClientSubscription)

    @Query("DELETE FROM client_subscriptions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM client_subscriptions")
    suspend fun getCount(): Int
}
