package co.adityarajput.notifilter.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromActiveTime(pair: Pair<Int, Int>): String =
        "${pair.first},${pair.second}"

    @TypeConverter
    fun toActiveTime(value: String): Pair<Int, Int> =
        value.split(",").let { (start, end) -> start.toInt() to end.toInt() }

    @TypeConverter
    fun fromActiveDays(set: Set<Int>): String =
        set.joinToString(",")

    @TypeConverter
    fun toActiveDays(value: String): Set<Int> =
        value.split(",").map { it.toInt() }.toSet()
}
