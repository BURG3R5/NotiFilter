package co.adityarajput.notifilter.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import co.adityarajput.notifilter.data.filter.Filter
import co.adityarajput.notifilter.data.filter.FilterDao
import co.adityarajput.notifilter.data.notification.Notification
import co.adityarajput.notifilter.data.notification.NotificationDao

@Database(
    entities = [Filter::class, Notification::class],
    version = 7,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(5, 6),
        AutoMigration(6, 7, NotiFilterDatabase.DeleteTableAN::class),
    ],
)
@TypeConverters(Converters::class)
abstract class NotiFilterDatabase : RoomDatabase() {
    abstract fun filterDao(): FilterDao
    abstract fun notificationDao(): NotificationDao

    @DeleteTable("active_notifications")
    class DeleteTableAN : AutoMigrationSpec

    companion object {
        @Volatile
        private var instance: NotiFilterDatabase? = null

        fun getDatabase(context: Context): NotiFilterDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, NotiFilterDatabase::class.java, "notifilter_database")
                    .build().also { instance = it }
            }
        }
    }
}
