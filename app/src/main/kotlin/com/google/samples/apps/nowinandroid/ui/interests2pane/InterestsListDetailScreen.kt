/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.ui.interests2pane

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.samples.apps.nowinandroid.feature.interests.InterestsRoute
import com.google.samples.apps.nowinandroid.feature.interests.navigation.INTERESTS_ROUTE
import com.google.samples.apps.nowinandroid.feature.interests.navigation.TOPIC_ID_ARG
import com.google.samples.apps.nowinandroid.feature.topic.TopicDetailPlaceholder
import com.google.samples.apps.nowinandroid.feature.topic.navigation.TOPIC_ROUTE
import com.google.samples.apps.nowinandroid.feature.topic.navigation.createTopicRoute
import com.google.samples.apps.nowinandroid.feature.topic.navigation.navigateToTopic
import com.google.samples.apps.nowinandroid.feature.topic.navigation.topicScreen
import java.util.UUID

private const val DETAIL_PANE_NAVHOST_ROUTE = "detail_pane_route"
// 这里定义有点乱啊，路由名字是interests_route，但关联组件叫 InterestsListDetailScreen
fun NavGraphBuilder.interestsListDetailScreen() {
    composable(
        route = INTERESTS_ROUTE,
        arguments = listOf(
            navArgument(TOPIC_ID_ARG) {
                type = NavType.StringType
                defaultValue = null
                nullable = true
            },
        ),
    ) {
        // 包含主题列表及明细屏幕，主从组件
        InterestsListDetailScreen()
    }
}

@Composable
internal fun InterestsListDetailScreen(
    viewModel: Interests2PaneViewModel = hiltViewModel(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    // 获取当前选中的topicId
    // 可观察状态字段
    val selectedTopicId by viewModel.selectedTopicId.collectAsStateWithLifecycle()
    // 当selectedTopicId发生变化时会重新渲染组件InterestsListDetailScreen
    InterestsListDetailScreen(
        selectedTopicId = selectedTopicId,
        onTopicClick = viewModel::onTopicClick,
        windowAdaptiveInfo = windowAdaptiveInfo,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun InterestsListDetailScreen(
    selectedTopicId: String?,
    onTopicClick: (String) -> Unit,
    windowAdaptiveInfo: WindowAdaptiveInfo,
) {
    // 用于管理多窗格布局的导航
    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator(
        // 计算推荐的布局方式
        scaffoldDirective = calculatePaneScaffoldDirective(windowAdaptiveInfo),
        // 初始导航栈历史，如果有2个，则会显示Detail，如果只有1个则只会显示List
        initialDestinationHistory = listOfNotNull(
            // List路由导航
            ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
            // Detail路由导航
            ThreePaneScaffoldDestinationItem<Nothing>(ListDetailPaneScaffoldRole.Detail)
                // 只要满足条件就返回this值，即添加这个Detail路由导航
                .takeIf {
                selectedTopicId != null
            },
        ),
    )
    // 监听返回事件
    BackHandler(listDetailNavigator.canNavigateBack()) {
        listDetailNavigator.navigateBack()
    }

    // 详情指定开始路由，当selectedTopicId发生变化时重新创建可观察可变状态
    // remember函数，可携带参数Key,当KEY发生变化时，会重新计算代码块并缓存计算的值
    // 下次组件重新渲染时，发现KEY没有变，则直接读取上次缓存的值返回
    // 在Composable组件内，创建可变状态必须用remember包装
    var nestedNavHostStartDestination by remember {
        // ::createTopicRoute是函数引用
        // 如果selectedTopicId不为空，则会调用函数createTopicRoute(selectedTopicId)并返回结果
        mutableStateOf(selectedTopicId?.let(::createTopicRoute) ?: TOPIC_ROUTE)
    }


    // 创建嵌套导航KEY
    var nestedNavKey by rememberSaveable(
        // stateSaver 实际上就是如何序列化与反序列化状态值
        stateSaver = Saver(
            // 提供保存函数：即如何将原值转换后缓存起来
            { it.toString()},
            // 提供恢复函数：从缓存中恢复原值
            UUID::fromString),
    ) {
        // 创建一个Composable组件可观察的可变状态
        mutableStateOf(UUID.randomUUID())
    }
    Log.d("nestedNavKey", nestedNavKey.toString())

    // 使用key函数，当nestedNavKey值不变时将不会调用key的Lambda表达式
    val nestedNavController = key(nestedNavKey) {
        rememberNavController()
    }
    // 点击主题显示详情明细
    fun onTopicClickShowDetailPane(topicId: String) {
        // 将当前主题ID存到ViewModel中
        onTopicClick(topicId)
        // 如果详情已显示
        if (listDetailNavigator.isDetailPaneVisible()) {
            // If the detail pane was visible, then use the nestedNavController navigate call
            // directly
            nestedNavController.navigateToTopic(topicId) {
                // 导航之前先清理与DETAIL_PANE_NAVHOST_ROUTE不同的返回栈，直到找到匹配的路由
                popUpTo(DETAIL_PANE_NAVHOST_ROUTE)
            }
        } else {
            // Otherwise, recreate the NavHost entirely, and start at the new destination
            // 重新创建主题开始路由
            nestedNavHostStartDestination = createTopicRoute(topicId)
            // 生成嵌套导航key
            nestedNavKey = UUID.randomUUID()
        }
        // 导航到详情页
        listDetailNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    // 如何控制显示list还是detail
    ListDetailPaneScaffold(
        value = listDetailNavigator.scaffoldValue,
        // 确定布局策略，如果是大屏幕则同时显示列表与详情
        // listDetailNavigator.scaffoldDirective.maxHorizontalPartitions=2,
        // 则同时显示List与Detail
        directive = listDetailNavigator.scaffoldDirective,
        listPane = {
            AnimatedPane {
                InterestsRoute(
                    onTopicClick = ::onTopicClickShowDetailPane,
                    highlightSelectedTopic = listDetailNavigator.isDetailPaneVisible(),
                )
            }
        },
        detailPane = {
            AnimatedPane {
                // 当key发生变化时需要动画过渡
                key(nestedNavKey) {
                    Log.d("nestedNavKey", nestedNavKey.toString())
                    // 当NavHost入参发生变化时才会创建新实例
                    // NavHost新实例会跳转到配置的startDestination路由中
                    NavHost(
                        navController = nestedNavController,
                        startDestination = nestedNavHostStartDestination,
                        // 明细面板路由，唯一标识这个NavHost
                        route = DETAIL_PANE_NAVHOST_ROUTE,
                    ) {
                        // 带路径路由
                        topicScreen(
                            showBackButton = !listDetailNavigator.isListPaneVisible(),
                            onBackClick = listDetailNavigator::navigateBack,
                            onTopicClick = ::onTopicClickShowDetailPane,
                        )
                        composable(route = TOPIC_ROUTE) {
                            // 主题路由占位符
                            TopicDetailPlaceholder()
                        }
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    // 当前导航布局值
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isDetailPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

