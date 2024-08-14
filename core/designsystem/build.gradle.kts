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
 *
 * plugins配置提供构建过程中的特定功能：编译打包、代码检查、语言支持等
 */
plugins {
    // 声明这是android库模块构建
    alias(libs.plugins.nowinandroid.android.library)
    // 添加对Jetpack Compose支持
    alias(libs.plugins.nowinandroid.android.library.compose)
    // 添加对覆盖率测试报告的支持
    alias(libs.plugins.nowinandroid.android.library.jacoco)
    // io.github.takahirom.roborazzi 是一个用于 Android 开发的 JetBrains 插件，主要用于自动化捕获和比较 UI 截图，
    // 以帮助开发者在开发过程中进行视觉回归测试。Roborazzi 插件通过集成到 Android Studio 中，
    // 提供了一种简便的方式来捕获应用程序在不同设备、屏幕尺寸和语言设置下的截图，并能自动检测和报告 UI 变化。
    alias(libs.plugins.roborazzi)
}

// android插件配置
android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.google.samples.apps.nowinandroid.core.designsystem"
}

dependencies {
    // 依赖自定义的质量检查库
    lintPublish(projects.lint)
    // api 声明的依赖，对于引用这个库模块的项目也被要求包含这些依赖，但会自动获取无需显示声明
    // implementation依赖只对当前模块的编译与运行有影响
    // 添加编译需要的Compose依赖包
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.navigationSuite)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.util)

    // 添加coil库支持，响应式图片加载库
    implementation(libs.coil.kt.compose)

    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.androidx.compose.ui.testManifest)
    
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)
    testImplementation(projects.core.screenshotTesting)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}
