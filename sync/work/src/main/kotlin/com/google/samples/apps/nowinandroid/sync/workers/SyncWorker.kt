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
 * 使用@HiltWorker说明这个类是可以被注入的Worker类
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    // @Assisted注解告知这个参数是由Worker框架提供的
    @Assisted private val appContext: Context,
    // Worker框架提供的注入参数
    @Assisted workerParams: WorkerParameters,
    // Hilt框架自动注入
    private val niaPreferences: NiaPreferencesDataSource,
    // 主题仓库，基于版本号同步更新
    private val topicRepository: TopicsRepository,
    private val newsRepository: NewsRepository,
    private val searchContentsRepository: SearchContentsRepository,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val analyticsHelper: AnalyticsHelper,
    private val syncSubscriber: SyncSubscriber,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    // 这个方法要做什么
    // 同步时在前台发出的通知
    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        // traceAsync有什么作用，配置方法名 sync又是何用
        traceAsync("Sync", 0) {
            // 开始日志事件
            analyticsHelper.logSyncStarted()
            // 同步订阅，如果是prod环境，要连接firebase云服务进行消息订阅服务
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
                // 具体的同步业务是在各自仓库中调用的，所以这里要调用仓库提供的sync方法
                // 但是同步作业任务中需要处理各个仓库通用处理部分，比如读取或设置当前版本号
                // 数据同步是基于版本号来管理的，所以作业任务要实现Synchronizer提供的与版本号有关的接口
                // 仓库实现的是Syncable接口的syncWith方法，为什么能直接调用sync方法？？？

                // 因为当前类实现了接口Synchronizer提供的方法，同时在接口Synchronizer中添加了Syncable的
                // 扩展函数sync，所以仓库这里才能调用sync方法
                // 其中sync方法再调用了Syncable的syncWith函数，同时传入Synchronizer接口实例
                // 为什么要传入Synchronizer实例呢？
                // 因为在Synchronizer提供了扩展函数封装数据同步逻辑处理，那么在各个仓库就可以直接调用这个
                // 接口提供的扩展函数，仓库只要传入各自的业务实现，通用的数据同步逻辑委托给Synchronizer接口实现

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

    // 在类里定义单例对象，使用companion声明，使外部可以直接通过类来访问所定义的函数
    companion object {
        /**
         * Expedited one time work to sync data on app startup
         * App启动时需要快速同步数据
         * 不明白为什么要通过委托类Worker来动态创建SyncWorker，直接调用不是一样吗？
         * 为什么要这样绕弯呢？
         * 难道使用DelegatingWorker只是一种示例吗？
         * 创建一个worker代理类，让代理类来动态创建
         * 通过setInputData动态传递Data，告知要调用哪个实现类
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

        fun startUpSyncWork2() = OneTimeWorkRequestBuilder<SyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(SyncConstraints)
                .build()

    }
}
