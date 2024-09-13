/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.topic

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaBackground
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaFilterChip
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.core.ui.TrackScrollJank
import com.google.samples.apps.nowinandroid.core.ui.UserNewsResourcePreviewParameterProvider
import com.google.samples.apps.nowinandroid.core.ui.userNewsResourceCardItems
import com.google.samples.apps.nowinandroid.feature.topic.R.string

@Composable
internal fun TopicRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TopicViewModel = hiltViewModel(),
) {
    // 主题信息UI状态，响应式流
    val topicUiState: TopicUiState by viewModel.topicUiState.collectAsStateWithLifecycle()
    // 主题下的新闻列表UI状态，响应式流
    val newsUiState: NewsUiState by viewModel.newsUiState.collectAsStateWithLifecycle()

    // 追踪屏幕事件
    TrackScreenViewEvent(screenName = "Topic: ${viewModel.topicId}")
    // 单个主题窗口
    TopicScreen(
        // 主题信息
        topicUiState = topicUiState,
        // 当前主题下的所有新闻资源
        newsUiState = newsUiState,
        modifier = modifier.testTag("topic:${viewModel.topicId}"),
        // 是否显示返回按钮
        showBackButton = showBackButton,
        // 返回事件
        onBackClick = onBackClick,
        // 关注事件
        onFollowClick = viewModel::followTopicToggle,
        // 收藏事件
        onBookmarkChanged = viewModel::bookmarkNews,
        // 是否已读事件
        onNewsResourceViewed = { viewModel.setNewsResourceViewed(it, true) },
        // 主题点击事件
        onTopicClick = onTopicClick,
    )
}

@VisibleForTesting
@Composable
internal fun TopicScreen(
    topicUiState: TopicUiState,
    newsUiState: NewsUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onFollowClick: (Boolean) -> Unit,
    onTopicClick: (String) -> Unit,
    onBookmarkChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 懒加载列表状态，也是使用rememberSaveable实现
    val state = rememberLazyListState()
    // 追踪UI渲染 性能指标
    TrackScrollJank(scrollableState = state, stateName = "topic:screen")
    // 外层使用Box容器
    Box(
        modifier = modifier,
    ) {
        // 使用懒加载列
        LazyColumn(
            // 引入状态
            state = state,
            // 水平居中
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 注意这里的Lambda体内部的代码都应包装在LazyListScope上下文中
            // 有了这个上下文就方便封装组件在别处了，但封装后的组件也只能应用于这个上下文中
            // 添加一行，占位符
            // 注意Item上下文是LazyListScope
            // 为什么要加Item呢？因为LazyColumn Lambda作用域是LazyListScope
            // 所以Spacer需要写在包含LazyListScope这个作用域的组件内才可以
            item {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
            }
            when (topicUiState) {
                // 加载框也是要放在Item组件内
                TopicUiState.Loading -> item {
                    // 主题正在加载中时显示对应加载中组件，有点复杂 todo...
                    NiaLoadingWheel(
                        modifier = modifier,
                        contentDesc = stringResource(id = string.feature_topic_loading),
                    )
                }
                // 主题加载失败异常未处理
                TopicUiState.Error -> TODO()
                // 主题成功
                is TopicUiState.Success -> {
                    // 再添加一行主题菜单栏，显示返回按钮与关注按钮
                    // 关注按钮是有状态的
                    // 添加单个栏目
                    item {
                        TopicToolbar(
                            showBackButton = showBackButton,
                            onBackClick = onBackClick,
                            onFollowClick = onFollowClick,
                            uiState = topicUiState.followableTopic,
                        )
                    }
                    // 显示
                    topicBody(
                        name = topicUiState.followableTopic.topic.name,
                        description = topicUiState.followableTopic.topic.longDescription,
                        news = newsUiState,
                        imageUrl = topicUiState.followableTopic.topic.imageUrl,
                        onBookmarkChanged = onBookmarkChanged,
                        onNewsResourceViewed = onNewsResourceViewed,
                        onTopicClick = onTopicClick,
                    )
                }
            }
            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        // 以下是添加垂直方向快速滚动条
        val itemsAvailable = topicItemsSize(topicUiState, newsUiState)
        val scrollbarState = state.scrollbarState(
            itemsAvailable = itemsAvailable,
        )
        state.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical,
            onThumbMoved = state.rememberDraggableScroller(
                itemsAvailable = itemsAvailable,
            ),
        )
    }
}

private fun topicItemsSize(
    topicUiState: TopicUiState,
    newsUiState: NewsUiState,
) = when (topicUiState) {
    TopicUiState.Error -> 0 // Nothing
    TopicUiState.Loading -> 1 // Loading bar
    is TopicUiState.Success -> when (newsUiState) {
        NewsUiState.Error -> 0 // Nothing
        NewsUiState.Loading -> 1 // Loading bar
        is NewsUiState.Success -> 2 + newsUiState.news.size // Toolbar, header
    }
}

private fun LazyListScope.topicBody(
    name: String,
    description: String,
    news: NewsUiState,
    imageUrl: String,
    onBookmarkChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
) {
    // TODO: Show icon if available
    // 添加单个栏目
    item {
        // 占一行显示主题图片与描述
        TopicHeader(name, description, imageUrl)
    }

    // 用户新闻资源列表用卡片显示
    // 入参是新闻列表 + 收藏事件 + 已读事件 + 点击主题事件
    userNewsResourceCards(news, onBookmarkChanged, onNewsResourceViewed, onTopicClick)
}

@Composable
private fun TopicHeader(name: String, description: String, imageUrl: String) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        // 显示动态异步图片
        DynamicAsyncImage(
            imageUrl = imageUrl,
            contentDescription = null,
            modifier = Modifier
                // 图片水平居中显示
                .align(Alignment.CenterHorizontally)
                // 直接给图片设置大小，设置最大小最小宽高
                .size(216.dp)
                .padding(bottom = 12.dp),
        )
        // Text样式使用typography预定义样式
        Text(name, style = MaterialTheme.typography.displayMedium)
        if (description.isNotEmpty()) {
            Text(
                description,
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

// TODO: Could/should this be replaced with [LazyGridScope.newsFeed]?
private fun LazyListScope.userNewsResourceCards(
    news: NewsUiState,
    onBookmarkChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
) {
    // 因为新闻资源列表是用状态包装的，所以需要用到when来处理UI
    when (news) {
        // 资源收集成功
        is NewsUiState.Success -> {
            userNewsResourceCardItems(
                items = news.news,
                onToggleBookmark = { onBookmarkChanged(it.id, !it.isSaved) },
                onNewsResourceViewed = onNewsResourceViewed,
                onTopicClick = onTopicClick,
                itemModifier = Modifier.padding(24.dp),
            )
        }

        // 正在加载中
        is NewsUiState.Loading -> item {
            NiaLoadingWheel(contentDesc = "Loading news") // TODO
        }

        // 异常
        else -> item {
            Text("Error") // TODO
        }
    }
}

@Preview
@Composable
private fun TopicBodyPreview() {
    NiaTheme {
        LazyColumn {
            topicBody(
                name = "Jetpack Compose",
                description = "Lorem ipsum maximum",
                news = NewsUiState.Success(emptyList()),
                imageUrl = "",
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
                onTopicClick = {},
            )
        }
    }
}

@Composable
private fun TopicToolbar(
    uiState: FollowableTopic,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
    onFollowClick: (Boolean) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        if (showBackButton) {
            // 显示返回按钮
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = NiaIcons.ArrowBack,
                    contentDescription = stringResource(
                        id = com.google.samples.apps.nowinandroid.core.ui.R.string.core_ui_back,
                    ),
                )
            }
        } else {
            // Keeps the NiaFilterChip aligned to the end of the Row.
            Spacer(modifier = Modifier.width(1.dp))
        }
        val selected = uiState.isFollowed
        // 显示是否关注的组件
        NiaFilterChip(
            selected = selected,
            onSelectedChange = onFollowClick,
            modifier = Modifier.padding(end = 24.dp),
        ) {
            if (selected) {
                Text("FOLLOWING")
            } else {
                Text("NOT FOLLOWING")
            }
        }
    }
}

@DevicePreviews
@Composable
fun TopicScreenPopulated(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NiaTheme {
        NiaBackground {
            TopicScreen(
                topicUiState = TopicUiState.Success(userNewsResources[0].followableTopics[0]),
                newsUiState = NewsUiState.Success(userNewsResources),
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
                onTopicClick = {},
            )
        }
    }
}

@DevicePreviews
@Composable
fun TopicScreenLoading() {
    NiaTheme {
        NiaBackground {
            TopicScreen(
                topicUiState = TopicUiState.Loading,
                newsUiState = NewsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
                onTopicClick = {},
            )
        }
    }
}
