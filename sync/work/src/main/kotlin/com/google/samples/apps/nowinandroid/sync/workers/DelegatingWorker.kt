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
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * An entry point to retrieve the [HiltWorkerFactory] at runtime
 * 运行时提取HiltWorkerFactory实例
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to
 * out说明是协变，可以接受任何 CoroutineWorker 的子类
 * KClass代表是kotlin的反射
 * 即可以给CoroutineWorker子类添加扩展函数delegatedData
 * 实现：给某个类的所有子类添加扩展方法
 */
internal fun KClass<out CoroutineWorker>.delegatedData() =
    Data.Builder()
        // qualifiedName 是完整类名
        .putString(WORKER_CLASS_NAME, qualifiedName)
        .build()

/**
 * A worker that delegates sync to another [CoroutineWorker] constructed with a [HiltWorkerFactory].
 * 动态创建 worker
 * This allows for creating and using [CoroutineWorker] instances with extended arguments
 * without having to provide a custom WorkManager configuration that the app module needs to utilize.
 *
 * In other words, it allows for custom workers in a library module without having to own
 * configuration of the WorkManager singleton.
 */
class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    // 获取真实Worker类名
    private val workerClassName =
        workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    // 根据传入的workerClass动态创建worker
    private val delegateWorker =
        // 通过EntryPointAccessors来获取HiltWorkerFactoryEntryPoint实例，从而拿到hiltWorkerFactory工厂
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            .hiltWorkerFactory()
            .createWorker(appContext, workerClassName, workerParams)
            as? CoroutineWorker
            ?: throw IllegalArgumentException("Unable to find appropriate worker")

    override suspend fun getForegroundInfo(): ForegroundInfo =
        delegateWorker.getForegroundInfo()

    override suspend fun doWork(): Result =
        // 调用真实worker的doWork方法
        delegateWorker.doWork()
}
