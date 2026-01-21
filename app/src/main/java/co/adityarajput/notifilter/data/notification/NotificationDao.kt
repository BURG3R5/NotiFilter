package co.adityarajput.notifilter.data.notification

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Upsert
    suspend fun upsert(vararg notification: Notification)

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun list(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Int

    @Query("DELETE FROM notifications WHERE id IN (SELECT id FROM notifications ORDER BY timestamp ASC LIMIT :count)")
    suspend fun trim(count: Int)
}
