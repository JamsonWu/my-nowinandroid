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

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@HiltAndroidTest
class SyncWorkerTest {

    // 定义最外层测试规则：当测试运行时会应用所设置的规则，这个规则是实现依赖注入功能
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)


    // context与targetcontext对比分析
    // targetContext：这里是指被测试应用的上下文，主要用于访问目标应的资源文件，当需要模拟用户操作时才需要这个
    // private val mcontext
    //    get() = InstrumentationRegistry.getInstrumentation().targetContext

    // 定义应用上下文属性 context：这里是指测试应用本身的上下文，用于代码测试
    private val context
        get() = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        // 初始化测试用的WorkManager
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testSyncWork() {
        // Create request
        // 创建一次性的工作任务：可以定义任务的执行条件、输入数据、约束条件等
        val request = SyncWorker.startUpSyncWork()

        val workManager = WorkManager.getInstance(context)
        // 测试驱动主要用于设置在测试上下文忽略一些约束条件，即测试时默认相关条件都满足
        // 由于getTestDriver返回值是可空对象，所以这里要求进行强制转为非空，当为空时会报空指针
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        // Enqueue and wait for result.
        // 将一次性的工作任务发给workManager进行调度执行
        // result是Future对象代表异步计算结果
        // 入队执行等待入队结果：阻塞当前线程直到入队完成
        workManager.enqueue(request).result.get()

        // Get WorkInfo and outputData
        // 阻塞当前线程，直到获取到入队信息，此时任务已入队但未被执行
        val preRunWorkInfo = workManager.getWorkInfoById(request.id).get()

        // Assert 校验
        assertEquals(WorkInfo.State.ENQUEUED, preRunWorkInfo.state)

        // Tells the testing framework that the constraints have been met
        // 告知当前测试环境满足所有约束条件，即不会验证相关设置的约束条件
        testDriver.setAllConstraintsMet(request.id)

        // 再次获取链路上的WorkInfo
        // 阻塞当前线程，入队在之前已完成同时任务应该已经开始运行，读取当前WorkInfo
        val postRequirementWorkInfo = workManager.getWorkInfoById(request.id).get()
        // 判断任务是否正在运行中
        assertEquals(WorkInfo.State.RUNNING, postRequirementWorkInfo.state)
    }
}
