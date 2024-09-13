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

package com.google.samples.apps.nowinandroid.feature.bookmarks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.google.samples.apps.nowinandroid.feature.bookmarks.BookmarksRoute

// 路由地址配置
const val BOOKMARKS_ROUTE = "bookmarks_route"
// 导航到收藏页面
fun NavController.navigateToBookmarks(navOptions: NavOptions) = navigate(BOOKMARKS_ROUTE, navOptions)

fun NavGraphBuilder.bookmarksScreen(
    // 回调函数：打开主题列表，即打开指定主题的Interests页面，带参数 topicId
    onTopicClick: (String) -> Unit,
    // 异步回调函数：此函数主要作用是显示提示框，这个提示框可以添加一个按钮事件进行自定义处理，在这个模块主要作用是取消删除收藏功能
    onShowSnackbar: suspend (String, String?) -> Boolean,
) {
    composable(route = BOOKMARKS_ROUTE) {
        // 打开收藏路由组件，注意命名借鉴，这里命名为XXXRoute
        BookmarksRoute(onTopicClick, onShowSnackbar)
    }
}
