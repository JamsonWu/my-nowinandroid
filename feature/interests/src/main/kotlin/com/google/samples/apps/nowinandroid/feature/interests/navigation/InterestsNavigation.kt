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

package com.google.samples.apps.nowinandroid.feature.interests.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.samples.apps.nowinandroid.feature.interests.InterestsRoute

const val TOPIC_ID_ARG = "topicId"
const val INTERESTS_ROUTE_BASE = "interests_route"
const val INTERESTS_ROUTE = "$INTERESTS_ROUTE_BASE?$TOPIC_ID_ARG={$TOPIC_ID_ARG}"

// 兴趣菜单，默认是显示主题列表，当选中某个主题时，则显示当前主题的新闻列表
fun NavController.navigateToInterests(topicId: String? = null, navOptions: NavOptions? = null) {
    val route = if (topicId != null) {
        // 带参数路由
        "${INTERESTS_ROUTE_BASE}?${TOPIC_ID_ARG}=$topicId"
    } else {
        INTERESTS_ROUTE_BASE
    }
    navigate(route, navOptions)
}
// 下面的定义暂时没用
fun NavGraphBuilder.interestsScreen(
    onTopicClick: (String) -> Unit,
) {
    composable(
        // 允许带参数路由
        route = INTERESTS_ROUTE,
        arguments = listOf(
            // 路由参数字段名
            navArgument(TOPIC_ID_ARG) {
                // 默认值为空
                defaultValue = null
                // 允许为空
                nullable = true
                // 类型是字符串
                type = NavType.StringType
            },
        ),
    ) {
        InterestsRoute(onTopicClick = onTopicClick)
    }
}
