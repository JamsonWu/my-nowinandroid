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

package com.google.samples.apps.nowinandroid.feature.foryou.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.samples.apps.nowinandroid.feature.foryou.ForYouRoute

const val LINKED_NEWS_RESOURCE_ID = "linkedNewsResourceId"
// $LINKED_NEWS_RESOURCE_ID读取变量值
// {$LINKED_NEWS_RESOURCE_ID}代表路由参数，路由参数字段名为${LINKED_NEWS_RESOURCE_ID}
//
const val FOR_YOU_ROUTE = "for_you_route/{$LINKED_NEWS_RESOURCE_ID}"
private const val DEEP_LINK_URI_PATTERN =
    "https://www.nowinandroid.apps.samples.google.com/foryou/{$LINKED_NEWS_RESOURCE_ID}"

// 在NavController添加扩展函数
fun NavController.navigateToForYou(navOptions: NavOptions) {
    // navigate(FOR_YOU_ROUTE, navOptions)
    // 为什么加上 navOptions 就无法获取到参数 abc123，原因是navOptions中配置了saveState=true，所以无法读取最新值
    // navigate("for_you_route/abc123", navOptions)
    // 菜单事件导航代码封装在NiaAppState中配置菜单的代码中
    navigate(FOR_YOU_ROUTE,navOptions)
}
// 在NavController添加扩展函数
// 添加路由配置，这个事件如何传入？
// forYouScreen是NavGraphBuilder的扩展函数，而composable也是composable的扩展函数
// 所以forYouScreen函数可以直接调用composable函数
fun NavGraphBuilder.forYouScreen(onTopicClick: (String) -> Unit) {
    // 添加 composable 到 NavGraphBuilder 中
    // 路由配置 + Composable组件
    composable(
        // 带路径路由用 /{linkedNewsResourceId}
        route = FOR_YOU_ROUTE,
        // 链接配置
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        // 路由参数
        arguments = listOf(
            // 指定路由参数字段名 LINKED_NEWS_RESOURCE_ID
            navArgument(LINKED_NEWS_RESOURCE_ID) { type = NavType.StringType },
        ),
    ) {
        // 发现
        // back ->
        // val param = back.arguments?.getString(LINKED_NEWS_RESOURCE_ID)
        // 组件如何读取路径参数值?
        ForYouRoute(onTopicClick)
    }
}



