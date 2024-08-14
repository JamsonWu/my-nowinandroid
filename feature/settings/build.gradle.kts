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
    alias(libs.plugins.nowinandroid.android.feature)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.library.jacoco)
}

android {
    namespace = "com.google.samples.apps.nowinandroid.feature.settings"
}

dependencies {
    // 兼容旧版本的android设备，AppCompat库提供了ActionBar、Toolbar、Fragment等组件的向后兼容实现，以及主题和样式资源
    implementation(libs.androidx.appcompat)
    // 是Google Play服务中的一个库，它主要用于帮助Android开发者在应用中展示所使用的开源软件（OSS）的许可证信息。
    implementation(libs.google.oss.licenses)
    // 依赖本地子项目data
    implementation(projects.core.data)
    testImplementation(projects.core.testing)
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}
