package co.adityarajput.notifilter.data.filter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(filter: Filter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAll(filters: List<Filter>)

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
