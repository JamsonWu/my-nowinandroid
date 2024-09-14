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

package com.google.samples.apps.nowinandroid.sync.status

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import com.google.samples.apps.nowinandroid.core.data.util.SyncManager
import com.google.samples.apps.nowinandroid.sync.initializers.SYNC_WORK_NAME
import com.google.samples.apps.nowinandroid.sync.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [SyncManager] backed by [WorkInfo] from [WorkManager]
 *
 */
internal class WorkManagerSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncManager {
    // 从WorkManager获取当前同步的状态，判断是否正在同步中
    override val isSyncing: Flow<Boolean> =
        // getWorkInfosForUniqueWorkFlow 返回作业任务链中WorkInfo
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            // 检查WorkInfo列表中是否有正在运行中的状态
            .map(List<WorkInfo>::anyRunning)
            .conflate()

    // 这个方法提供给消息接收时调用
    override fun requestSync() {
        // 基于Context创建后台任务管理器 WorkManger
        val workManager = WorkManager.getInstance(context)
        // Run sync on app startup and ensure only one sync worker runs at any time
        // 创建唯一作业任务
        workManager.enqueueUniqueWork(
            // 定义一个同步任务名称，通过这个名称可以知道作业任务的实时状态
            SYNC_WORK_NAME,
            // 如果已存在同名的任务，则什么也不做，让原先任务继续
            ExistingWorkPolicy.KEEP,
            // 具体作业任务
            SyncWorker.startUpSyncWork(),
        )
    }
}
// 使用 any 来检查列表是否至少有一项是满足条件的
private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }
