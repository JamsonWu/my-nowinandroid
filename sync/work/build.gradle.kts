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
plugins {
    // android库模块支持
    alias(libs.plugins.nowinandroid.android.library)
    // 代码测试覆盖率报告支持
    alias(libs.plugins.nowinandroid.android.library.jacoco)
    // 添加依赖注入框架支持
    alias(libs.plugins.nowinandroid.hilt)
}

android {
    defaultConfig {
        testInstrumentationRunner = "com.google.samples.apps.nowinandroid.core.testing.NiaTestRunner"
    }
    namespace = "com.google.samples.apps.nowinandroid.sync"
}

dependencies {
    // ksp 根据注解自动生成代码，依赖注入框架hilt使用它来生成自动绑定代码
    ksp(libs.hilt.ext.compiler)
    // 追踪与诊断应用性能工具
    implementation(libs.androidx.tracing.ktx)
    // 对WorkManager框架支持
    implementation(libs.androidx.work.ktx)
    // hilt库扩展添加对WorkManager的技
    implementation(libs.hilt.ext.work)
    implementation(projects.core.analytics)
    implementation(projects.core.data)
    implementation(projects.core.notifications)

    prodImplementation(libs.firebase.cloud.messaging)
    prodImplementation(platform(libs.firebase.bom))

    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kotlinx.coroutines.guava)
    androidTestImplementation(projects.core.testing)
}
