package vip.smartfamily.vfs.app_executors

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import kotlin.coroutines.coroutineContext

/**
 * Created by Sumbum on 2018/2/27.
 */
@SuppressLint("Registered")
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        @Volatile
        lateinit var appContext: Context
    }
}