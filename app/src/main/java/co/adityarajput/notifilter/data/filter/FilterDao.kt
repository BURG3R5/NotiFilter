package co.adityarajput.notifilter.data.filter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(filter: Filter)

    @Query("SELECT * from filters ORDER BY packageName ASC")
    fun list(): Flow<List<Filter>>

    @Query("UPDATE filters SET hits = hits + 1 WHERE id = :id")
    suspend fun registerHit(id: Int)

    @Delete
    suspend fun delete(filter: Filter)
}
