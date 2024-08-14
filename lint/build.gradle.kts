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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 以下配置，说明代码是java与kotlin混合
plugins {
    // 为java库项目提供标准配置，如何打包及依赖管理等
    `java-library`
    // 在java虚拟机上编译kotlin代码
    kotlin("jvm")
    // 配置针对什么代码库做检查，应用还是库模块
    alias(libs.plugins.nowinandroid.android.lint)
}

java {
    // Up to Java 11 APIs are available through desugaring
    // https://developer.android.com/studio/write/java11-minimal-support-table
    // java向下兼容配置
    // 源码兼容JAVA11，即源码可以用JAVA11引入的新特性，那么JAVA源码就不能使用高于JAVA11的新特性
    sourceCompatibility = JavaVersion.VERSION_11
    // 编译后的字节码与JAVA11虚拟机兼容，比JAVA11低的版本虚拟机可能就无法运行了
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

// 隔离编译阶段与测试阶段
dependencies {
    // 编译阶段，项目中可能有kotlin代码，编译时需要
    compileOnly(libs.kotlin.stdlib)
    // 编译阶段需要，在编译阶段代码质量检查
    compileOnly(libs.lint.api)
    // 测试阶段，提供代码质量检查
    testImplementation(libs.lint.checks)
    // 测试阶段，与测试有关的
    testImplementation(libs.lint.tests)
    // 测试阶段，允许使用kotlin编写的测试代码
    testImplementation(kotlin("test"))
}
