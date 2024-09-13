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

package com.google.samples.apps.nowinandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.metrics.performance.JankStats
import com.google.samples.apps.nowinandroid.MainActivityUiState.Loading
import com.google.samples.apps.nowinandroid.MainActivityUiState.Success
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.analytics.LocalAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand
import com.google.samples.apps.nowinandroid.core.ui.LocalTimeZone
import com.google.samples.apps.nowinandroid.ui.NiaApp
import com.google.samples.apps.nowinandroid.ui.rememberNiaAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"
// 告知这个组件是入口点，APP启动时第一个创建的组件
// Dagger Hilt 提供依赖注入管理
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Lazily inject [JankStats], which is used to track jank throughout the app.
     * JankStats用户追踪App UI渲染性能问题
     * 帧渲染过长的会记录日志
     * lateinit延迟注入，使用时再依赖注入
     */
    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    // 报告网络连接状态
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    // 监听时区改变，这个时区改变通知有什么用？
    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    // 提供事件记录日志
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    // 用户新闻仓库，获取新闻仓库
    @Inject
    lateinit var userNewsResourceRepository: UserNewsResourceRepository

    // 实例化viewModel
    // 使用by有委托功能，viewMode属性实际上是委托 viewModels函数来实现，
    // 背后是通过ViewModelProvider来创建ViewModel实例
    // 还可以实现懒加载功能，当首次使用viewModel属性时才会实例化
    val viewModel: MainActivityViewModel by viewModels()


    // savedInstanceState 这个实例状态主要保存什么值，主要作用？
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 定义ui状态，默认值是Loading
        // 这里只是定义一个当前ui可变状态，状态可重新赋值ui
        var uiState: MainActivityUiState by mutableStateOf(Loading)
        // Update the uiState
        // 与宿主生命周期（Activity或Fragment）相关联的协程上下文
        // 使用lifecycleScope可自动管理协程的开启与取消
        // 当activity销毁时会自动取消协程，对流的订阅也会取消
        // lifecycleScope是Activity协程上下文，提供协程功能
        // 注意：看过去 lifecycleScope与lifecycle.repeatOnLifecycle组合达到了与
        // val isOffline by appState.isOffline.collectAsStateWithLifecycle() 相同目的
        // 但是collectAsStateWithLifecycle只能用于Composable组件内
        lifecycleScope.launch {
            // 当宿主生命周期处于STARTED状态时重复执行
            // 当activity处于STARTED状态时更新一次ui状态值
            // lifecycle是Activity提供的生命周期相关函数
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 更新 uiState
                viewModel.uiState
                    // 当viewModel中的uiState发射新值时，会触发onEach返回新流
                    .onEach {
                        // 当StateFlow收到新流数据时，则触发这个函数
                        // 初始流也会发射一次，流数据读取成功后也会发射一次
                        // 所以这里会发射2次流数据，第一次是Loading
                        uiState = it
                    }
                    // 开启订阅StateFlow流
                    .collect()
            }

        }


        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        // 控制启动画面是否处于显示状态
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                Loading -> true
                is Success -> false
            }
        }

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations, and go edge-to-edge
        // This also sets up the initial system bar style based on the platform theme
        enableEdgeToEdge()

        // 这是根视图：当应用程序的状态发生变更时，会从setContent这里开始检查看哪些@Composable组件
        // 与这个变更的状态有关，只重新渲染与这个变更状态有关的组件
        setContent {
            // 是否深色主题
            val darkTheme = shouldUseDarkTheme(uiState)
            // Update the edge to edge configuration to match the theme
            // This is the same parameters as the default enableEdgeToEdge call, but we manually
            // resolve whether or not to show dark theme using uiState, since it can be different
            // than the configuration's dark theme value based on the user preference.
            // 执行带副作用的，当darkTheme发生变化时会执行一次
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {
                    // 执行Effect主体代码之前会先调用这里，主要目的是为了资源释放
                }
            }

            // app状态包括网络状态监听，用户新闻仓库，时区
            val appState = rememberNiaAppState(
                networkMonitor = networkMonitor,
                userNewsResourceRepository = userNewsResourceRepository,
                timeZoneMonitor = timeZoneMonitor,
            )

            val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper,
                LocalTimeZone provides currentTimeZone,
            ) {
                NiaTheme(
                    darkTheme = darkTheme,
                    androidTheme = shouldUseAndroidTheme(uiState),
                    disableDynamicTheming = shouldDisableDynamicTheming(uiState),
                ) {
                    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
                    NiaApp(appState)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }
}

/**
 * Returns `true` if the Android theme should be used, as a function of the [uiState].
 */
@Composable
private fun shouldUseAndroidTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    Loading -> false
    is Success -> when (uiState.userData.themeBrand) {
        ThemeBrand.DEFAULT -> false
        ThemeBrand.ANDROID -> true
    }
}

/**
 * Returns `true` if the dynamic color is disabled, as a function of the [uiState].
 */
@Composable
private fun shouldDisableDynamicTheming(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    Loading -> false
    is Success -> !uiState.userData.useDynamicColor
}

/**
 * Returns `true` if dark theme should be used, as a function of the [uiState] and the
 * current system context.
 */
@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    // 由于Loading是单例对象所以可不用加 is
    Loading -> isSystemInDarkTheme()
    is Success -> when (uiState.userData.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
