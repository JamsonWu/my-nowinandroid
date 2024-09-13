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

package com.google.samples.apps.nowinandroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.bookmarksScreen
//import com.google.samples.apps.nowinandroid.feature.foryou.navigation.FOR_YOU_ROUTE
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.forYouScreen
import com.google.samples.apps.nowinandroid.feature.interests.navigation.interestsScreen
import com.google.samples.apps.nowinandroid.feature.interests.navigation.navigateToInterests
import com.google.samples.apps.nowinandroid.feature.search.navigation.searchScreen
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.INTERESTS
import com.google.samples.apps.nowinandroid.ui.NiaAppState
import com.google.samples.apps.nowinandroid.ui.interests2pane.interestsListDetailScreen

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun NiaNavHost(
    appState: NiaAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    // startDestination: String = FOR_YOU_ROUTE,
    // 配置开始路由
    startDestination: String = "for_you_route/{linkedNewsResourceId}",
) {
    val navController = appState.navController
    // 这里主要是配置顶级菜单路由
    // 不同的模块在不同的地方进行详细配置
    // 只配置了4个路由
    NavHost(
        navController = navController,
        // 设置开始路由，进入App时首先会打开这个开始路由
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // NavHost最后一个参数是builder: NavGraphBuilder.() -> Unit
        // 这里方法体的内容要求在NavGraphBuilder上下文中
        // 而forYouScreen是NavGraphBuilder的扩展函数，所以可以直接调用
        // navController::navigateToInterests是传递实例方法引用，需要时再调用
        // 实际上就是传递回调函数而已，这是kotlin的一种语法
        // 定义foryou顶级路由
        //  导航菜单1
        // onTopicClick是什么位置
        // forYouScreen代码存于功能模块feature的子模块foryou中
        // 代码组成：Navigation + Screen + ViewModel + UiState
        //         配置路由  +    页面   +  状态管理   + UI所用到的字段定义
        // foryou页面点击主题时需要跳转到感兴趣页面
        forYouScreen(onTopicClick = navController::navigateToInterests)
        // 定义书签路由
        // 导航菜单2
        bookmarksScreen(
            onTopicClick = navController::navigateToInterests,
            onShowSnackbar = onShowSnackbar,
        )
        // 定义查询路由
        // 头部菜单左侧的查询按钮
        searchScreen(
            onBackClick = navController::popBackStack,
            onInterestsClick = {
                // 当查询不到时跳转
                appState.navigateToTopLevelDestination(INTERESTS)
                               },
            onTopicClick = navController::navigateToInterests,
        )
        // 定义兴趣路由
        // 导航菜单3
        // 为啥能写成这么复杂的代码呢？
        // 包含主题列表与主题明细列表两个UI，主从组件
        // 大屏幕会自适应同时显示List与Detail
        interestsListDetailScreen()
    }
}
