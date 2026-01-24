package co.adityarajput.notifilter.data

import androidx.room.TypeConverter
import co.adityarajput.notifilter.data.models.Action

class Converters {
    @TypeConverter
    fun fromAction(action: Action) = action.toString()

    @TypeConverter
    fun toAction(value: String) = Action.fromString(value)

    @TypeConverter
    fun fromDays(days: Set<Int>) = days.joinToString(",")

    @TypeConverter
    fun toDays(value: String) = value.split(",").map { it.toInt() }.toSet()
}
