/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vip.smartfamily.vfs.app_executors

import android.os.Handler
import android.os.Looper
import vip.smartfamily.vfs.app_executors.AppExecutors
import vip.smartfamily.vfs.app_executors.NameNetThreadFactory
import java.util.concurrent.*

/**
 * 全局执行器池，用于整个应用程序。
 *
 *
 * 像这样分组任务避免了任务饥饿的影响（例如磁盘读取不会在WebService请求后面等待）。
 */
class AppExecutors private constructor(private val mDiskIO: Executor, private val mNetworkIO: Executor, private val mMainThread: Executor) {
    fun diskIO(): Executor {
        return mDiskIO
    }

    fun networkIO(): Executor {
        return mNetworkIO
    }

    fun mainThread(): Executor {
        return mMainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    companion object {
        private var executors: AppExecutors? = null

        /*public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3),
                new MainThreadExecutor());
    }*/
        val instance: AppExecutors
            get() {
                if (executors == null) {
                    val factory = NameNetThreadFactory()
                    val executor = ThreadPoolExecutor(
                            4,
                            55,
                            210,
                            TimeUnit.MILLISECONDS,
                            LinkedBlockingQueue(55),
                            factory,
                            ThreadPoolExecutor.CallerRunsPolicy()
                    )
                    executor.allowCoreThreadTimeOut(true)
                    executors = AppExecutors(Executors.newSingleThreadExecutor(), executor,
                            MainThreadExecutor())
                }
                return executors!!
            }
        private val factory = NameNetThreadFactory()
    }
}