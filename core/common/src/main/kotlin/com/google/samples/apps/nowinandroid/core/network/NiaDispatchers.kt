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

package com.google.samples.apps.nowinandroid.core.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

// @Qualifier 用来区分具有相同类型但功能不同的bean，常用于依赖注入框架
// 类型相同但又需要功能不同的实例时，就要加上这个注解
@Qualifier
// 在运行时可以反射访问
@Retention(RUNTIME)
// 带参数注解，不同参数对应不同的实例
annotation class Dispatcher(val niaDispatcher: NiaDispatchers)

// 枚举类，提供调度策略标识
enum class NiaDispatchers {
    Default,
    IO,
}
