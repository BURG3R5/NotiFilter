package co.adityarajput.notifilter.data.active_notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveNotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(notification: ActiveNotification)

    @Query("SELECT * FROM active_notifications")
    fun list(): Flow<List<ActiveNotification>>

    @Query("SELECT COUNT(*) FROM active_notifications")
    suspend fun count(): Int

    @Query("DELETE FROM active_notifications WHERE id IN (SELECT id FROM active_notifications ORDER BY timestamp ASC LIMIT :count)")
    suspend fun trim(count: Int)

    @Query("DELETE FROM active_notifications WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM active_notifications")
    suspend fun deleteAll()
}
