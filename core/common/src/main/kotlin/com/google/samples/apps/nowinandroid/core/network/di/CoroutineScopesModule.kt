/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.network.di

import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.Default
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
// 加上这个注解，依赖注入时就会去寻找包含这个注解的实例
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
internal object CoroutineScopesModule {
    @Provides
    @Singleton
    // 依赖注入时，只要加上这个@ApplicationScope注解，就会找这个实例
    @ApplicationScope
    fun providesCoroutineScope(
        // 依赖注入，由于用了注解 @Dispatcher(Default)，所以会去找包含这个注解的CoroutineDispatcher实例
        // 标注协程调度策略
        @Dispatcher(Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(
                // 创建一个 SupervisorJob 实例，它作为取消上下文
        SupervisorJob()
                // + 运算符在这里用于合并两个 CoroutineContext 元素。
                +
                // 提供一个 CoroutineDispatcher 实例，它决定了协程的调度策略
                dispatcher)
}
