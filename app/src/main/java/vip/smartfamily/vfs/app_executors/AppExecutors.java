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

package vip.smartfamily.vfs.app_executors;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 全局执行器池，用于整个应用程序。
 * <p>
 * 像这样分组任务避免了任务饥饿的影响（例如磁盘读取不会在WebService请求后面等待）。
 */
public class AppExecutors {
    private static AppExecutors executors = null;

    private final Executor mDiskIO;

    private final Executor mNetworkIO;

    private final Executor mMainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;
        this.mMainThread = mainThread;
    }

    /*public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3),
                new MainThreadExecutor());
    }*/

    public static AppExecutors getInstance() {
        if (executors == null) {
            NameNetThreadFactory factory = new NameNetThreadFactory();

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    4,
                    55,
                    210,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(55),
                    factory,
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executor.allowCoreThreadTimeOut(true);

            executors = new AppExecutors(Executors.newSingleThreadExecutor(), executor,
                    new MainThreadExecutor());
        }
        return executors;
    }

    public Executor diskIO() {
        return mDiskIO;
    }

    public Executor networkIO() {
        return mNetworkIO;
    }

    public Executor mainThread() {
        return mMainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    private static NameNetThreadFactory factory = new NameNetThreadFactory();
}