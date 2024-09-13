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

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    // 用于编译和生成java与kotlin的源代码
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.google.samples.apps.nowinandroid.core.datastore.proto"
}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        // 设置 protoc 编译器版本
        artifact = libs.protobuf.protoc.get().toString()
    }
    // 生成代码规则
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    // 生成轻量级java类
                    option("lite")
                }
                register("kotlin") {
                    // 生成轻量级kotlin类
                    option("lite")
                }
            }
        }
    }
}

// 设置编译后生成的源码目录
androidComponents.beforeVariants {
    android.sourceSets.register(it.name) {
        val buildDir = layout.buildDirectory.get().asFile
        java.srcDir(buildDir.resolve("generated/source/proto/${it.name}/java"))
        kotlin.srcDir(buildDir.resolve("generated/source/proto/${it.name}/kotlin"))
    }
}
// 编译依赖插件
dependencies {
    api(libs.protobuf.kotlin.lite)
}
