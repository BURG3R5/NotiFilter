package co.adityarajput.notifilter.data.filter

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Upsert
    suspend fun upsert(vararg filters: Filter)

    @Query("SELECT * from filters ORDER BY packageName ASC")
    fun list(): Flow<List<Filter>>

    @Query("UPDATE filters SET hits = hits + 1 WHERE id = :id")
    suspend fun registerHit(id: Int)

    @Query("UPDATE filters SET historyEnabled = 1 - historyEnabled WHERE id = :id")
    suspend fun toggleHistory(id: Int)

    @Query("UPDATE filters SET enabled = 1 - enabled WHERE id = :id")
    suspend fun toggleEnabled(id: Int)

    @Delete
    suspend fun delete(filter: Filter)

    @Query("DELETE FROM filters")
    suspend fun deleteAll()
}
