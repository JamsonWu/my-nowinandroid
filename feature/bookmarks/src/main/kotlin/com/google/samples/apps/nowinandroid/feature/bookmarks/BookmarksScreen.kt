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

package com.google.samples.apps.nowinandroid.feature.bookmarks

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.google.samples.apps.nowinandroid.core.designsystem.theme.LocalTintTheme
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Loading
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Success
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.core.ui.TrackScrollJank
import com.google.samples.apps.nowinandroid.core.ui.UserNewsResourcePreviewParameterProvider
import com.google.samples.apps.nowinandroid.core.ui.newsFeed

@Composable
internal fun BookmarksRoute(
    onTopicClick: (String) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    // 在路由页引入ViewModel
    viewModel: BookmarksViewModel = hiltViewModel(),
) {

    // 收藏的新闻列表
    // 在路由组件内定义来自于ViewModel提供的可观察状态
    val feedState by viewModel.feedUiState.collectAsStateWithLifecycle()
    // 当feedState变更时会重新渲染组件BookmarksScreen
    // 在路由组件内调用窗口组件
    // 窗口组件入参：
    //           可观察状态字段，当状态变化时窗口组件会重新渲染
    //           回调事件：弹窗提示+主题点击跳转事件
    //           回调事件：引用ViewModel中定义的函数，删除收藏+取消删除收藏+取消删除状态，注意引用函数格式是 viewModel::函数名
    //           ViewModel内使用mutableStateOf自定义了一个可观察状态字段shouldDisplayUndoBookmark，用于控制提示框
    BookmarksScreen(
        feedState = feedState,
        onShowSnackbar = onShowSnackbar,
        // 删除收藏事件
        removeFromBookmarks = viewModel::removeFromSavedResources,
        // 设置新闻已浏览过
        onNewsResourceViewed = { viewModel.setNewsResourceViewed(it, true) },
        // 点击主题事件
        onTopicClick = onTopicClick,
        modifier = modifier,
        // 是否应显示取消删除收藏，在ViewModel中定义一个可观察状态
        shouldDisplayUndoBookmark = viewModel.shouldDisplayUndoBookmark,
        // 取消删除收藏回调函数
        undoBookmarkRemoval = viewModel::undoBookmarkRemoval,
        // 清除undo状态，那么就无法取消删除资源
        clearUndoState = viewModel::clearUndoState,
    )
}

/**
 * Displays the user's bookmarked articles. Includes support for loading and empty states.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun BookmarksScreen(
    feedState: NewsFeedUiState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    removeFromBookmarks: (String) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldDisplayUndoBookmark: Boolean = false,
    undoBookmarkRemoval: () -> Unit = {},
    clearUndoState: () -> Unit = {},
) {
    // 提示信息来自于在res->values->strings.xml中定义
    val bookmarkRemovedMessage = stringResource(id = R.string.feature_bookmarks_removed)
    val undoText = stringResource(id = R.string.feature_bookmarks_undo)

    // shouldDisplayUndoBookmark是否应显示取消收藏提示状态变更时
    // BookmarksScreen组件会重新渲染
    // 当状态变更时会重新执行LaunchedEffect中的Lambda表达式
    // 通过让ViewModel中的状态变更来触发UI组件的弹窗提示
    // 这里的弹窗提示还是封装在LaunchedEffect函数内

    // 使用LaunchedEffect函数目的是只有当shouldDisplayUndoBookmark状态发生变化时才会执行
    LaunchedEffect(shouldDisplayUndoBookmark) {
        if (shouldDisplayUndoBookmark) {
            //
            val snackBarResult = onShowSnackbar(bookmarkRemovedMessage, undoText)
            if (snackBarResult) {
                // 手动点击了事件，这里是反向操作，取消收藏反向操作，即取消上一步操作
                undoBookmarkRemoval()
            } else {
                clearUndoState()
            }
        }
    }
    // 组件生命周期事件：
    // 加载页面经历事件： CREATE,START,RESUME
    // 关闭页面经历事件：STOP,PAUSE
    //    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
    //        val c = 1
    //    }
    //    LifecycleEventEffect(Lifecycle.Event.ON_START) {
    //        val a = 2
    //    }
    //    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    //        val b = 3
    //    }
    // 在Composable组件中监听生命周期中的事件
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        // 组件退出时要移除上次的删除状态
        clearUndoState()
    }
    //    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    //        val c = 5
    //    }

    when (feedState) {
        Loading -> LoadingState(modifier)
        // feedState加载成功同时记录非空
        is Success -> if (feedState.feed.isNotEmpty()) {
            // 显示收藏表格组件
            BookmarksGrid(
                feedState,
                removeFromBookmarks,
                onNewsResourceViewed,
                onTopicClick,
                modifier,
            )
        } else {
            // 状态为空时显示内容
            EmptyState(modifier)
        }
    }

    TrackScreenViewEvent(screenName = "Saved")
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    NiaLoadingWheel(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .testTag("forYou:loading"),
        contentDesc = stringResource(id = R.string.feature_bookmarks_loading),
    )
}

@Composable
private fun BookmarksGrid(
    feedState: NewsFeedUiState,
    removeFromBookmarks: (String) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 定义一个交错表格的状态
    val scrollableState = rememberLazyStaggeredGridState()
    // 跟踪滚动状态的性能指标
    TrackScrollJank(scrollableState = scrollableState, stateName = "bookmarks:grid")
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // 使用垂直方向布局的交错表格来显示新闻列表
        LazyVerticalStaggeredGrid(
            // 一行显示多少列，根据宽度来自适应
            columns = StaggeredGridCells.Adaptive(300.dp),
            // 内容内边距
            contentPadding = PaddingValues(16.dp),
            // 水平方向间距
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // 垂直方向Item间距
            verticalItemSpacing = 24.dp,
            state = scrollableState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("bookmarks:feed"),
        ) {
            // 这里要调用通用的UI组件库，在core->ui中定义的组件
            // 实现新闻列表的渲染
            newsFeed(
                feedState = feedState,
                onNewsResourcesCheckedChanged = { id, _ -> removeFromBookmarks(id) },
                onNewsResourceViewed = onNewsResourceViewed,
                onTopicClick = onTopicClick,
            )
            // 使用Item占一行，设置安全边界的高度，避免内容超出屏幕范围无法正常显示
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
        // 以下是设置垂直方向滚动条，主要作用是可以拉动滚动条快速移动
        val itemsAvailable = when (feedState) {
            Loading -> 1
            is Success -> feedState.feed.size
        }
        val scrollbarState = scrollableState.scrollbarState(
            itemsAvailable = itemsAvailable,
        )
        scrollableState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical,
            onThumbMoved = scrollableState.rememberDraggableScroller(
                itemsAvailable = itemsAvailable,
            ),
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .testTag("bookmarks:empty"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val iconTint = LocalTintTheme.current.iconTint
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.feature_bookmarks_img_empty_bookmarks),
            colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_empty_error),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_empty_description),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
private fun LoadingStatePreview() {
    NiaTheme {
        LoadingState()
    }
}

@Preview
@Composable
private fun BookmarksGridPreview(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        BookmarksGrid(
            feedState = Success(userNewsResources),
            removeFromBookmarks = {},
            onNewsResourceViewed = {},
            onTopicClick = {},
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    NiaTheme {
        EmptyState()
    }
}
