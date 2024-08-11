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

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // 指定版本目录
    versionCatalogs {
        create("libs") {
            // 来自于上级目录下的libs.versions.toml的文件配置
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

// 当前项目名称
rootProject.name = "build-logic"
// 包含子项目，子项目自定义插件及注册插件
include(":convention")
