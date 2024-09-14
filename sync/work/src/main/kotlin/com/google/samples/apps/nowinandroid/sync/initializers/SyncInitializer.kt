/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.sync.initializers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.google.samples.apps.nowinandroid.sync.workers.SyncWorker

// 单例对象
object Sync {
    // This method is initializes sync, the process that keeps the app's data current.
    // It is called from the app module's Application.onCreate() and should be only done once.
    // 定义初始化方法
    fun initialize(context: Context) {
        // 获取WorkManager实例
        WorkManager.getInstance(context).apply {
            // 使用apply，{}内的作用域是WorkManager
            // Run sync on app startup and ensure only one sync worker runs at any time
            // App启动时唯一任务入队
            enqueueUniqueWork(
                // 任务名称
                SYNC_WORK_NAME,
                // 如果已存在，则忽略
                ExistingWorkPolicy.KEEP,
                // 一次性任务请求
                SyncWorker.startUpSyncWork(),
            )
        }

        // 链式调用，与上面一种方法是等效的
        //        WorkManager.getInstance(context)
        //            // Run sync on app startup and ensure only one sync worker runs at any time
        //            // 创建唯一作业任务
        //            .enqueueUniqueWork(
        //                // 定义一个同步任务名称，通过这个名称可以知道作业任务的实时状态
        //                SYNC_WORK_NAME,
        //                // 如果已存在同名的任务，则什么也不做，让原先任务继续
        //                ExistingWorkPolicy.KEEP,
        //                // 具体作业任务
        //                SyncWorker.startUpSyncWork(),
        //            )
    }
}

// This name should not be changed otherwise the app may have concurrent sync requests running
internal const val SYNC_WORK_NAME = "SyncWorkName"
