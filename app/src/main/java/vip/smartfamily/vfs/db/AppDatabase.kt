package vip.smartfamily.vfs.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import vip.smartfamily.vfs.app_executors.MyApplication
import vip.smartfamily.vfs.db.dao.SmbConDao
import vip.smartfamily.vfs.db.entity.SmbConInfo

@Database(
        entities = [
            SmbConInfo::class
        ],
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getSmbConDao(): SmbConDao

    companion object {
        @VisibleForTesting
        private val DATABASE_NAME = "vfs-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return instance ?: synchronized(AppDatabase::class.java) {
                instance ?: buildDatabase(MyApplication.appContext).also { instance = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            /*val contentValues = ContentValues()
                            contentValues.put("teamName", "未绑定")

                            db.insert(
                                    "connection_info",
                                    SQLiteDatabase.CONFLICT_REPLACE,
                                    contentValues
                            )*/
                            /*AppExecutors.getInstance().diskIO().execute{
                                val database = getInstance(appContext)
                                database.getDeviceSetting().init(DeviceSetting())
                            }*/
                        }
                    })
                    //.addMigrations(MIGRATION_1_3)
                    .build()
        }
    }
}