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
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.jacoco)
    alias(libs.plugins.nowinandroid.hilt)
    id("kotlinx-serialization")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "com.google.samples.apps.nowinandroid.core.network"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // 配置不同环境后台服务地址
    buildTypes {
        debug {
            secrets {
                propertiesFileName = "secrets.debug.properties"
            }
        }
        release {
            secrets {
                propertiesFileName = "secrets.release.properties"
            }
        }
    }
}
// secrets插件确实支持从多种来源读取敏感信息，包括环境变量和系统属性。
// 这提供了一种灵活的方式来处理敏感数据，尤其是在自动化构建和部署流程中。
// 密钥配置，相关配置参数在属性文件中定义，编译后会添加到BuildConfig对象中
secrets {
    defaultPropertiesFileName = "secrets.defaults.properties"
}

dependencies {
    api(libs.kotlinx.datetime)
    api(projects.core.common)
    api(projects.core.model)

    implementation(libs.coil.kt)
    implementation(libs.coil.kt.svg)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)

    testImplementation(libs.kotlinx.coroutines.test)
}
