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

package com.google.samples.apps.nowinandroid.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme

/**
 * Now in Android navigation bar item with icon and label content slots. Wraps Material 3
 * [NavigationBarItem].
 * 自定义封装导航栏目组件：自定义导航栏目颜色 + 入参传入选中与未选中图标
 * 常见导航栏主要包括底部导航栏，侧边导航栏
 * 导航栏每个栏目的定义是使用组件 NavigationBarItem
 * @param selected Whether this item is selected.
 * @param onClick The callback to be invoked when this item is selected.
 * @param icon The item icon content.
 * @param modifier Modifier to be applied to this item.
 * @param selectedIcon The item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun RowScope.NiaNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    // 默认图标
    icon: @Composable () -> Unit,
    // 被选中图标
    selectedIcon: @Composable () -> Unit = icon,
    // label文字
    label: @Composable (() -> Unit)? = null,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        // 改变导航栏目默认颜色配置
        colors = NavigationBarItemDefaults.colors(
            // 选中时图标颜色
            selectedIconColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            // 图标未选中时颜色
            unselectedIconColor = NiaNavigationDefaults.navigationContentColor(),
            // 文字选中时颜色
            selectedTextColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            // 文字未选中时颜色
            unselectedTextColor = NiaNavigationDefaults.navigationContentColor(),
            // 指示器颜色
            indicatorColor = NiaNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

/**
 * Now in Android navigation bar with content slot. Wraps Material 3 [NavigationBar].
 * 自定义导航栏，改变默认内容颜色（文字+图标）
 * @param modifier Modifier to be applied to the navigation bar.
 * @param content Destinations inside the navigation bar. This should contain multiple
 * [NavigationBarItem]s.
 */
@Composable
fun NiaNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        // 改变导航栏内容颜色
        contentColor = NiaNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

/**
 * Now in Android navigation rail item with icon and label content slots. Wraps Material 3
 * [NavigationRailItem].
 * 自定义侧边栏栏目样式，类似底部导航栏栏目
 * @param selected Whether this item is selected.
 * @param onClick The callback to be invoked when this item is selected.
 * @param icon The item icon content.
 * @param modifier Modifier to be applied to this item.
 * @param selectedIcon The item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun NiaNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NiaNavigationDefaults.navigationContentColor(),
            selectedTextColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NiaNavigationDefaults.navigationContentColor(),
            indicatorColor = NiaNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

/**
 * Now in Android navigation rail with header and content slots. Wraps Material 3 [NavigationRail].
 * 自定义侧边导航栏，主要改变容器颜色与内容颜色
 * @param modifier Modifier to be applied to the navigation rail.
 * @param header Optional header that may hold a floating action button or a logo.
 * @param content Destinations inside the navigation rail. This should contain multiple
 * [NavigationRailItem]s.
 */
@Composable
fun NiaNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        // 容器颜色使用透明
        containerColor = Color.Transparent,
        contentColor = NiaNavigationDefaults.navigationContentColor(),
        header = header,
        content = content,
    )
}

/**
 * Now in Android navigation suite scaffold with item and content slots.
 * Wraps Material 3 [NavigationSuiteScaffold].
 * 根据窗口大小等信息来决定生成什么样的导航布局，主要是改变导航栏颜色及传入自适应窗口信息
 * App主要是使用这个配置导航栏
 * @param modifier Modifier to be applied to the navigation suite scaffold.
 * @param navigationSuiteItems A slot to display multiple items via [NiaNavigationSuiteScope].
 * @param windowAdaptiveInfo The window adaptive info.
 * @param content The app content inside the scaffold.
 */
@OptIn(
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun NiaNavigationSuiteScaffold(
    // 导航栏目列表
    navigationSuiteItems: NiaNavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    // 窗口信息
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit,
) {
    // 根据自适应窗口信息决定布局类型
    // 根据当前窗口信息获取布局类型：实际只提供了底部与左侧菜单栏
    val layoutType = NavigationSuiteScaffoldDefaults
        .calculateFromAdaptiveInfo(windowAdaptiveInfo)
    // 定义导航栏目颜色
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        // 配置底部导航栏栏目颜色
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NiaNavigationDefaults.navigationContentColor(),
            selectedTextColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NiaNavigationDefaults.navigationContentColor(),
            indicatorColor = NiaNavigationDefaults.navigationIndicatorColor(),
        ),
        // 配置左边导航栏栏目颜色
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NiaNavigationDefaults.navigationContentColor(),
            selectedTextColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NiaNavigationDefaults.navigationContentColor(),
            indicatorColor = NiaNavigationDefaults.navigationIndicatorColor(),
        ),
        // 配置左上角抽屉导航栏栏目颜色
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NiaNavigationDefaults.navigationContentColor(),
            selectedTextColor = NiaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NiaNavigationDefaults.navigationContentColor(),
        ),
    )

    // 根据布局类型创建导航布局栏
    // 根据 layoutType生成侧边栏或底部菜单栏
    NavigationSuiteScaffold(
        // 菜单列表
        // 需要NavigationSuiteScope的扩展函数
        // 但是navigationSuiteItems是自定义NiaNavigationSuiteScope的扩展函数
        // 需要进行一次转换
        navigationSuiteItems = {
            // 创建一个对象NiaNavigationSuiteScope实例，然后把这个实例的引用this
            // 传递给run的Lambda表达式中
            // 即navigationSuiteItems的Lambda表达式运行在NiaNavigationSuiteScope的上下文中
            // 当执行navigationSuiteItems的Lambda表达式时需要调用的Item方法
            // 实际就是调用NiaNavigationSuiteScope实例提供的Item方法

            // 另外一种理解：给navigationSuiteItems中的Lambda表达式提供一个实例的Item方法
            NiaNavigationSuiteScope(
                navigationSuiteScope = this,
                // 改变导航栏目颜色
                navigationSuiteItemColors = navigationSuiteItemColors,
            ).run(navigationSuiteItems)
        },

        // 布局方式
        layoutType = layoutType,
        // 容器颜色改为透明
        containerColor = Color.Transparent,
        // 改变导航栏颜色
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            // 改变导航栏内容颜色
            navigationBarContentColor = NiaNavigationDefaults.navigationContentColor(),
            // 改变左边导航栏容器颜色为透明
            navigationRailContainerColor = Color.Transparent,
        ),
        modifier = modifier,
    ) {
        content()
    }
}

/**
 * A wrapper around [NavigationSuiteScope] to declare navigation items.
 * 自定义导航具体栏目渲染，主要是同时传入选中与非选中图标及改变导航栏目颜色
 */
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
class NiaNavigationSuiteScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors,
) {
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable () -> Unit,
        selectedIcon: @Composable () -> Unit = icon,
        label: @Composable (() -> Unit)? = null,
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        icon = {
            if (selected) {
                selectedIcon()
            } else {
                icon()
            }
        },
        label = label,
        colors = navigationSuiteItemColors,
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
fun NiaNavigationBarPreview() {
    val items = listOf("For you", "Saved", "Interests")
    val icons = listOf(
        NiaIcons.UpcomingBorder,
        NiaIcons.BookmarksBorder,
        NiaIcons.Grid3x3,
    )
    val selectedIcons = listOf(
        NiaIcons.Upcoming,
        NiaIcons.Bookmarks,
        NiaIcons.Grid3x3,
    )

    NiaTheme {
        NiaNavigationBar {
            items.forEachIndexed { index, item ->
                NiaNavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun NiaNavigationRailPreview() {
    val items = listOf("For you", "Saved", "Interests")
    val icons = listOf(
        NiaIcons.UpcomingBorder,
        NiaIcons.BookmarksBorder,
        NiaIcons.Grid3x3,
    )
    val selectedIcons = listOf(
        NiaIcons.Upcoming,
        NiaIcons.Bookmarks,
        NiaIcons.Grid3x3,
    )

    NiaTheme {
        NiaNavigationRail {
            items.forEachIndexed { index, item ->
                NiaNavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

/**
 * Now in Android navigation default values.
 */
object NiaNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}
