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

import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
// 使用 Room 访问数据库Sqlite约定的插件配置
class AndroidRoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // 配置 Room插件 用于集成 Room数据库
            pluginManager.apply("androidx.room")
            // ksp插件用于在编译时生成代码，为Room实体与Dao生成代理类
            pluginManager.apply("com.google.devtools.ksp")

            // 配置 KSP扩展
            extensions.configure<KspExtension> {
                // 处理Room注解时生成kotlin代码
                arg("room.generateKotlin", "true")
            }

            // 配置Room 扩展
            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.、、
                // 配置 Room 数据库的 schema目录，Room database每个版本都配置一个 schema文件
                // 目的是为了使用Room能自动迁移
                // 数据库迁移应用声明是什么？当应用升级时，数据库结构也可能发生变化，那么Room使用schema配置来解决新旧数据库的迁移工作
                // 可使用 AutoMigration 注解
                // 简化了数据库版本控制的复杂性
                // $projectDir 代表当前项目根目录的绝对路径
                val myPath = "$projectDir/schemas"
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                // 添加Room运行时库，ktx扩展与ksp编译器
                // 其中libs在versionCatalogs中会配置，在settings.gradle.kts文件中
                add("implementation", libs.findLibrary("room.runtime").get())
                add("implementation", libs.findLibrary("room.ktx").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }
        }
    }
}