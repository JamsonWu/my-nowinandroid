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

package com.google.samples.apps.nowinandroid.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.google.samples.apps.nowinandroid.lint.designsystem.DesignSystemDetector

// 注册自定义代码质量检查规则
class NiaIssueRegistry : IssueRegistry() {

    // 配置代码检查包括哪些问题类型
    override val issues = listOf(
        // 对Compose不正确使用的检查，对Composable组件做了一些映射，使用时对此做检查
        DesignSystemDetector.ISSUE,
        // 测试方法命名格式化检查，遵循 given, when,then语法，即前置条件，触发行为，期望结果(比如 assertEquals)
        TestMethodNameDetector.FORMAT,
        // 测试方法命名前缀检查，测试方法开始为 test
        TestMethodNameDetector.PREFIX,
    )

    // Lint API 级别
    override val api: Int = CURRENT_API
    // Lint 最小 API级别
    override val minApi: Int = 12

    // 信息提供方配置
    override val vendor: Vendor = Vendor(
        vendorName = "Now in Android",
        feedbackUrl = "https://github.com/android/nowinandroid/issues",
        contact = "https://github.com/android/nowinandroid",
    )
}
