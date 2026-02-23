package co.adityarajput.notifilter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import co.adityarajput.notifilter.data.models.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Upsert
    suspend fun upsert(vararg notification: Notification)

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun list(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications ORDER BY timestamp ASC LIMIT :count")
    suspend fun listOldestN(count: Int): List<Notification>

    @Query("SELECT * FROM notifications WHERE showInHistory = 1 ORDER BY id DESC")
    fun history(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE showInWidget = 1 ORDER BY id DESC")
    fun widget(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Int

    @Delete
    suspend fun delete(notification: Notification)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
