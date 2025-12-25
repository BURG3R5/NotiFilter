package co.adityarajput.notifilter.data.filter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.adityarajput.notifilter.R

@Entity(tableName = "filters")
data class Filter(
    val packageName: String,
    val queryPattern: String,
    @ColumnInfo(defaultValue = "DISMISS")
    val action: Action,
    @ColumnInfo(defaultValue = "null")
    val buttonPattern: String? = null,
    val hits: Int = 0,
    @ColumnInfo(defaultValue = "1")
    val enabled: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

enum class Action(val displayString: Int, val descriptionString: Int) {
    DISMISS(R.string.dismiss_short, R.string.dismiss_long),
    TAP(R.string.tap_short, R.string.tap_long),
}
