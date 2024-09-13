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

package com.google.samples.apps.nowinandroid.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.Synchronizer
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.datastore.ChangeListVersions
import com.google.samples.apps.nowinandroid.core.datastore.NiaPreferencesDataSource
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.sync.initializers.SyncConstraints
import com.google.samples.apps.nowinandroid.sync.initializers.syncForegroundInfo
import com.google.samples.apps.nowinandroid.sync.status.SyncSubscriber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Syncs the data layer by delegating to the appropriate repository instances with
 * sync functionality.
 * 同步作业任务
 * 入参：使用hilt依赖注入
 * 同步任务实现了接口 Synchronizer 提供的2个方法
 * 依赖注入：
 *    topicRepository有3个实现如何知道这里是使用哪个版本呢？
 *       在DataModule模块中会进行绑定用哪个版本实现
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val niaPreferences: NiaPreferencesDataSource,
    // 主题仓库，基于版本号同步更新
    private val topicRepository: TopicsRepository,
    private val newsRepository: NewsRepository,
    private val searchContentsRepository: SearchContentsRepository,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val analyticsHelper: AnalyticsHelper,
    private val syncSubscriber: SyncSubscriber,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        // traceAsync有什么作用，配置方法名 sync又是何用
        traceAsync("Sync", 0) {
            // 开始日志事件
            analyticsHelper.logSyncStarted()
            // 同步订阅
            syncSubscriber.subscribe()

            // First sync the repositories in parallel
            // 并行执行同步仓库
            // awaitAll 等待多个异步任务的完成
            val syncedSuccessfully = awaitAll(
                // 同步topic仓库
                // 注意这里调用的是Syncable的扩展函数sync()
                // 仓库本身实现了Syncable的同步接口 syncWith
                // 为什么不直接用topicRepository.syncWith(this@SyncWorker)而是去使用sync扩展函数
                // 主要原因：  1、隐式传参，简化调用
                //           2. 使用扩展函数可达到封装的目的，便于扩展调用，比如错误处理等
                //           3. 不用传参，达到解藕目的
                // 发起协程调用
                async { topicRepository.sync() },
                // 同步消息仓库
                async { newsRepository.sync() },
            )
                // 检查所有异步任务的结果是否符合预期，如果都满足将返回true
                .all { it }

            // 同步事件完成
            analyticsHelper.logSyncFinished(syncedSuccessfully)

            if (syncedSuccessfully) {
                // 如果同步成功，则插入数据到表中
                searchContentsRepository.populateFtsData()
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    // 从本地 Preferences 获取变化列表当前的版本号
    override suspend fun getChangeListVersions(): ChangeListVersions =
        niaPreferences.getChangeListVersions()

    // 将当前版本号更新到本地 Preferences 中
    override suspend fun updateChangeListVersions(
        update: ChangeListVersions.() -> ChangeListVersions,
    ) = niaPreferences.updateChangeListVersion(update)

    companion object {
        /**
         * Expedited one time work to sync data on app startup
         * App启动时需要快速同步数据
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            // 设置任务紧急或重要程度，排队执行
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            // 设置约束：要求网络连接
            .setConstraints(SyncConstraints)
            // SyncWorker::class返回的是SyncWorker的反射对象，即 KClass<SyncWorker>
            // 注意不是类实例
            .setInputData(SyncWorker::class.delegatedData())
            .build()

        fun builderDemo() {
            val builder = OneTimeWorkRequestBuilder<SyncWorker>()
            builder
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(SyncConstraints)
                .build()
        }
    }
}
