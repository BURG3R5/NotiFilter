package co.adityarajput.notifilter.data

import android.content.Context
import androidx.room.*
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FilterDao
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationDao

@Database(
    entities = [Filter::class, Notification::class],
    version = 4,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
    ],
)
@TypeConverters(Converters::class)
abstract class NotiFilterDatabase : RoomDatabase() {

    abstract fun filterDao(): FilterDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var Instance: NotiFilterDatabase? = null

        fun getDatabase(context: Context): NotiFilterDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, NotiFilterDatabase::class.java, "notifilter_database")
                    .build().also { Instance = it }
            }
        }
    }
}
