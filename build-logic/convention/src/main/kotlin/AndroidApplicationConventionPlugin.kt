/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.google.samples.apps.nowinandroid.configureBadgingTasks
import com.google.samples.apps.nowinandroid.configureGradleManagedDevices
import com.google.samples.apps.nowinandroid.configureKotlinAndroid
import com.google.samples.apps.nowinandroid.configurePrintApksTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

// 自定插件，针对整个项目的插件
// AndroidApplication项目约定的插件配置
class AndroidApplicationConventionPlugin : Plugin<Project> {
    // 当插件被应用时，apply方法会被调用，传入目标项目对象project
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // 应用android的应用插件
                apply("com.android.application")
                // 应用了kotlin for android的插件，这是构建android kotlin的基础
                // 实际是启用kotlin支持
                apply("org.jetbrains.kotlin.android")
                // 应用lint代码质量检查插件
                apply("nowinandroid.android.lint")
                // 用于检测与警告不匹配的依赖版本
                apply("com.dropbox.dependency-guard")
            }

            // 配置 Application扩展
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                // 设置目标SDK版本
                defaultConfig.targetSdk = 34
                @Suppress("UnstableApiUsage")
                // 测试禁止动画
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)
            }
            // 配置 android组件扩展
            extensions.configure<ApplicationAndroidComponentsExtension> {
                configurePrintApksTask(this)
                configureBadgingTasks(extensions.getByType<BaseExtension>(), this)
            }
        }
    }

}
