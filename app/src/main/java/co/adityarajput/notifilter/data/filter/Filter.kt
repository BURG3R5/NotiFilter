package co.adityarajput.notifilter.data.filter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filters")
data class Filter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val queryPattern: String,
    val hits: Int = 0,
    @ColumnInfo(defaultValue = "1")
    val enabled: Boolean = true,
)
