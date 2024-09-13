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

package com.google.samples.apps.nowinandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import com.google.samples.apps.nowinandroid.core.ui.TrackDisposableJank
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.BOOKMARKS_ROUTE
import com.google.samples.apps.nowinandroid.feature.bookmarks.navigation.navigateToBookmarks
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.FOR_YOU_ROUTE
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.navigateToForYou
import com.google.samples.apps.nowinandroid.feature.interests.navigation.INTERESTS_ROUTE
import com.google.samples.apps.nowinandroid.feature.interests.navigation.navigateToInterests
import com.google.samples.apps.nowinandroid.feature.search.navigation.navigateToSearch
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.BOOKMARKS
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.FOR_YOU
import com.google.samples.apps.nowinandroid.navigation.TopLevelDestination.INTERESTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

// 函数返回一个类型，这个类型的实例是由remember中的keys来控制如何实例化
@Composable
fun rememberNiaAppState(
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    // 使用rememberNavController创建唯一导航实例，在生命周期内创建一个唯一实例
    navController: NavHostController = rememberNavController(),
): NiaAppState {
    // 监控路由变化时相关性能指标
    NavigationTrackingSideEffect(navController)
    // 在kotlin最后一行会被当作返回值，所以此处可以省略 return
    // 使用remember作用是记忆，当remember所有的keys不变时，直接返回上一次计算的值，当有key发生变化时
    // 会重新执行函数体代码，即重新实例化对象NiaAppState()
    // 但是onCreate会重新调用，这几个keys都是引用类型，为啥需要用到remember
    return remember(
        navController,
        coroutineScope,
        networkMonitor,
        userNewsResourceRepository,
        timeZoneMonitor,
    ) {
        NiaAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            userNewsResourceRepository = userNewsResourceRepository,
            timeZoneMonitor = timeZoneMonitor,
        )
    }
}


// 使用 @Stable 作用是当这个类实例状态发生变化不会引起UI重新渲染，提供性能
//
@Stable
class NiaAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
) {
    // 当前导航路由
    // 使用val + @Composable get() 这种方式主要目的是为了当属性值发生改变时会自动更新UI
    val currentDestination: NavDestination?
      // 由于currentBackStackEntryAsState只能在@Composable上下文调用
      // 所以要添加@Composable注解
      // 属性提供get方法
      @Composable get() =
          navController.currentBackStackEntryAsState().value?.destination

    // 当前顶级导航路由
    val currentTopLevelDestination: TopLevelDestination?
        // 由于currentDestination属性需要在@Composable上下文调用
        // 所以这里也要加 @Composable注解
        // 属性提供get方法
        @Composable get() =
            when (currentDestination?.route) {
                FOR_YOU_ROUTE -> FOR_YOU
                BOOKMARKS_ROUTE -> BOOKMARKS
                INTERESTS_ROUTE -> INTERESTS
                else -> null
        }


    // 网络实时离线状态，是StateFlow状态
    val isOffline = networkMonitor.isOnline
        // 调用反向函数
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route.
     * 从枚举中读取顶级路由列表
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * The top level destinations that have unread news resources.
     * 获取存在未读资源的顶级路由
     */
    val topLevelDestinationsWithUnreadResources: StateFlow<Set<TopLevelDestination>> =
        userNewsResourceRepository.observeAllForFollowedTopics()
            .combine(userNewsResourceRepository.observeAllBookmarked()) { forYouNewsResources, bookmarkedNewsResources ->
                // 返回非空集合，setOfNotNull可以传入不同的元素，但只返回非空的元素
                // 由于使用Set说明返回值不重复的
                setOfNotNull(
                    // any 如果至少有一个元素满足条件则返回true
                    // takeIf 函数体如果返回true，则返回 FOR_YOU，否则返回null
                    // 如果至少存在一条forYou的未读新闻，则返回FOR_YOU顶级路由
                    FOR_YOU.takeIf { forYouNewsResources.any { !it.hasBeenViewed } },
                    BOOKMARKS.takeIf { bookmarkedNewsResources.any { !it.hasBeenViewed } },
                )
            }
            .stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     * 导航到顶级路由（即顶级菜单）
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val dest = navController.graph.findStartDestination()
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items

                popUpTo(navController.graph.findStartDestination().id) {
                    // 当设置值为true，则：
                    // 保存路由状态，比如首次访问 "for_you_route/123"
                    // 那么后面 navigate("for_you_route/234")，新参数234是无法传入的，会
                    // 读取第一次的参数123
                    // 加上这行代码，从foryou与bookmark模块跳转到主题页面后，再重新点击foryou与bookmark路由将无法正常跳转了
                    // foryou路由跳转到topic路由，然后无法返回foryou路由问题 todo...
                    // saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                FOR_YOU -> navController.navigateToForYou(topLevelNavOptions)
                BOOKMARKS -> navController.navigateToBookmarks(topLevelNavOptions)
                INTERESTS -> navController.navigateToInterests(null, topLevelNavOptions)
            }
        }
    }

    fun navigateToSearch() = navController.navigateToSearch()
}

/**
 * Stores information about navigation events to be used with JankStats
 * 路由变化记录相关性能指标
 */
@Composable
private fun NavigationTrackingSideEffect(navController: NavHostController) {
    TrackDisposableJank(navController) { metricsHolder ->
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            metricsHolder.state?.putState("Navigation", destination.route.toString())
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}