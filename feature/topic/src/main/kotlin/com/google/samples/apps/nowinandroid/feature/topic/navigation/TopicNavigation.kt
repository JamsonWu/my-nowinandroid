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

package com.google.samples.apps.nowinandroid.feature.topic.navigation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.samples.apps.nowinandroid.feature.topic.TopicRoute
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

private val URL_CHARACTER_ENCODING = UTF_8.name()

@VisibleForTesting
// 定义路由路径参数名常量
internal const val TOPIC_ID_ARG = "topicId"
// 定义主题路由名字
const val TOPIC_ROUTE = "topic_route"

// 构建主题参数类
// 主题ID来自于savedStateHandle
internal class TopicArgs(val topicId: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            // 调用默认构造函数，传参 topicId
        this(URLDecoder.decode((savedStateHandle[TOPIC_ID_ARG]), URL_CHARACTER_ENCODING))
}
// 定义NavController扩展函数
fun NavController.navigateToTopic(topicId: String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    // 可以直接调用NavController作用域内的函数
    navigate(createTopicRoute(topicId)) {
        navOptions()
    }
}
// 使用URLEncoder构建路径路由
// 在字符串中读取变量值需要借助$符号
fun createTopicRoute(topicId: String): String {
    val encodedId = URLEncoder.encode(topicId, URL_CHARACTER_ENCODING)
    return "$TOPIC_ROUTE/$encodedId"
}

fun NavGraphBuilder.topicScreen(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
) {
    composable(
        route = "topic_route/{$TOPIC_ID_ARG}",
        arguments = listOf(
            navArgument(TOPIC_ID_ARG) { type = NavType.StringType },
        ),
    ) {
        // 单个主题路由
        TopicRoute(
            showBackButton = showBackButton,
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
        )
    }
}
