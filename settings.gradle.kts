/*
 * Copyright 2021 The Android Open Source Project
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
// 插件依赖版本声明
pluginManagement {
    // 这行代码告诉Gradle去包含位于build-logic目录下的另一个构建项目。
    // 这个子项目通常包含了共享的构建逻辑，如自定义插件、任务、配置等。
    includeBuild("build-logic")
    repositories {
        // google的maven仓库
        google()
        // 开源的maven仓库
        mavenCentral()
        // gradle插件仓库
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // 解析依赖项时的仓库模式。FAIL_ON_PROJECT_REPOS模式意味着Gradle将只使用在dependencyResolutionManagement
    // 块中显式声明的仓库来解析依赖项，而不会考虑项目级别的仓库定义。
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

// 根项目名称
rootProject.name = "nowinandroid"
// 加上这一行代码，引用子模块依赖时就可以直接使用 projects.core.data 这种简化方式来引用了
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// 子项目定义
include(":app")
include(":app-nia-catalog")
include(":benchmarks")
include(":core:analytics")
include(":core:common")
include(":core:data")
include(":core:data-test")
include(":core:database")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:datastore-test")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:notifications")
include(":core:screenshot-testing")
include(":core:testing")
include(":core:ui")

include(":feature:foryou")
include(":feature:interests")
include(":feature:bookmarks")
include(":feature:topic")
include(":feature:search")
include(":feature:settings")
include(":lint")
include(":sync:work")
include(":sync:sync-test")
include(":ui-test-hilt-manifest")
include(":mylibrary")
