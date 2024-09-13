/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.DraggableScrollbar
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.rememberDraggableScroller
import com.google.samples.apps.nowinandroid.core.designsystem.component.scrollbar.scrollbarState
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.InterestsItem
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Success
import com.google.samples.apps.nowinandroid.core.ui.R.string
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.core.ui.newsFeed
import com.google.samples.apps.nowinandroid.feature.search.R as searchR

// 查询路由组件
@Composable
internal fun SearchRoute(
    onBackClick: () -> Unit,
    // 当查询不到数据时，提示信息处提供跳转到主题栏目列表页
    onInterestsClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    // 读取最近查询条件UI状态
    val recentSearchQueriesUiState by searchViewModel.recentSearchQueriesUiState.collectAsStateWithLifecycle()
    // 返回查询结果UI状态
    val searchResultUiState by searchViewModel.searchResultUiState.collectAsStateWithLifecycle()
    // 获取查询条件
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    // 查询UI组件
    SearchScreen(
        // 传递布局管理，改变组件的行为与外观 ：布局调整，样式调整，交互性，动画效果等
        modifier = modifier,
        // 查询条件
        searchQuery = searchQuery,
        // 最近的查询条件
        recentSearchesUiState = recentSearchQueriesUiState,
        // 查询结果
        searchResultUiState = searchResultUiState,
        // 查询输入改变事件
        onSearchQueryChanged = searchViewModel::onSearchQueryChanged,
        // 保存最近的查询条件
        onSearchTriggered = searchViewModel::onSearchTriggered,
        // 清除最近的查询条件
        onClearRecentSearches = searchViewModel::clearRecentSearches,
        // 新闻资源收藏事件
        onNewsResourcesCheckedChanged = searchViewModel::setNewsResourceBookmarked,
        // 新闻资源已读事件
        onNewsResourceViewed = { searchViewModel.setNewsResourceViewed(it, true) },
        // 主题关注事件
        onFollowButtonClick = searchViewModel::followTopic,
        // 返回按钮事件
        onBackClick = onBackClick,
        // 当查询内容为空时指引跳转到主题栏目列表
        onInterestsClick = onInterestsClick,
        // 点击主题跳转事件
        onTopicClick = onTopicClick,
    )
}

@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    recentSearchesUiState: RecentSearchQueriesUiState = RecentSearchQueriesUiState.Loading,
    searchResultUiState: SearchResultUiState = SearchResultUiState.Loading,
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchTriggered: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit = { _, _ -> },
    onNewsResourceViewed: (String) -> Unit = {},
    onFollowButtonClick: (String, Boolean) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    onInterestsClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
) {
    // 追踪屏幕事件
    TrackScreenViewEvent(screenName = "Search")
    // 列布局
    Column(modifier = modifier) {
        // 占位符
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        // 查询工具栏
        SearchToolbar(
            onBackClick = onBackClick,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
        when (searchResultUiState) {
            SearchResultUiState.Loading,
            SearchResultUiState.LoadFailed,
            -> Unit

            SearchResultUiState.SearchNotReady -> SearchNotReadyBody()
            // 未输入查询条件，显示最近的查询条件列表
            SearchResultUiState.EmptyQuery,
            -> {
                if (recentSearchesUiState is RecentSearchQueriesUiState.Success) {
                    // 显示最近的查询条件内
                    RecentSearchesBody(
                        onClearRecentSearches = onClearRecentSearches,
                        onRecentSearchClicked = {
                            onSearchQueryChanged(it)
                            onSearchTriggered(it)
                        },
                        recentSearchQueries = recentSearchesUiState.recentQueries.map { it.query },
                    )
                }
            }

            is SearchResultUiState.Success -> {
                if (searchResultUiState.isEmpty()) {
                    // 查询结果为空时UI展示
                    EmptySearchResultBody(
                        searchQuery = searchQuery,
                        onInterestsClick = onInterestsClick,
                    )
                    if (recentSearchesUiState is RecentSearchQueriesUiState.Success) {
                        // 当查询结果为空时显示最近查询条件
                        RecentSearchesBody(
                            onClearRecentSearches = onClearRecentSearches,
                            onRecentSearchClicked = {
                                onSearchQueryChanged(it)
                                onSearchTriggered(it)
                            },
                            recentSearchQueries = recentSearchesUiState.recentQueries.map { it.query },
                        )
                    }
                } else {
                    // 显示查询结果
                    SearchResultBody(
                        // 查询条件
                        searchQuery = searchQuery,
                        // 主题列表
                        topics = searchResultUiState.topics,
                        // 新闻列表
                        newsResources = searchResultUiState.newsResources,
                        // 触发查询事件
                        onSearchTriggered = onSearchTriggered,
                        onTopicClick = onTopicClick,
                        onNewsResourcesCheckedChanged = onNewsResourcesCheckedChanged,
                        onNewsResourceViewed = onNewsResourceViewed,
                        onFollowButtonClick = onFollowButtonClick,
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
fun EmptySearchResultBody(
    searchQuery: String,
    onInterestsClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 48.dp),
    ) {
        val message = stringResource(id = searchR.string.feature_search_result_not_found, searchQuery)
        val start = message.indexOf(searchQuery)
        Text(
            text = AnnotatedString(
                text = message,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + searchQuery.length,
                    ),
                ),
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
        val interests = stringResource(id = searchR.string.feature_search_interests)
        val tryAnotherSearchString = buildAnnotatedString {
            append(stringResource(id = searchR.string.feature_search_try_another_search))
            append(" ")
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                pushStringAnnotation(tag = interests, annotation = interests)
                append(interests)
            }
            append(" ")
            append(stringResource(id = searchR.string.feature_search_to_browse_topics))
        }
        ClickableText(
            text = tryAnotherSearchString,
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                ),
            ),
            modifier = Modifier
                .padding(start = 36.dp, end = 36.dp, bottom = 24.dp)
                .clickable {},
        ) { offset ->
            tryAnotherSearchString.getStringAnnotations(start = offset, end = offset)
                .firstOrNull()
                ?.let { onInterestsClick() }
        }
    }
}

@Composable
private fun SearchNotReadyBody() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 48.dp),
    ) {
        Text(
            text = stringResource(id = searchR.string.feature_search_not_ready),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}

// 显示主题列表与新闻资源列表
@Composable
private fun SearchResultBody(
    searchQuery: String,
    topics: List<FollowableTopic>,
    newsResources: List<UserNewsResource>,
    onSearchTriggered: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onFollowButtonClick: (String, Boolean) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // 使用垂直不规则表格展示
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 24.dp,
            modifier = Modifier
                .fillMaxSize()
                .testTag("search:newsResources"),
            state = state,
        ) {
            // 显示主题列表
            if (topics.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = searchR.string.feature_search_topics))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                topics.forEach { followableTopic ->
                    val topicId = followableTopic.topic.id
                    item(
                        // Append a prefix to distinguish a key for news resources
                        key = "topic-$topicId",
                        span = StaggeredGridItemSpan.FullLine,
                    ) {
                        InterestsItem(
                            name = followableTopic.topic.name,
                            following = followableTopic.isFollowed,
                            description = followableTopic.topic.shortDescription,
                            topicImageUrl = followableTopic.topic.imageUrl,
                            onClick = {
                                // Pass the current search query to ViewModel to save it as recent searches
                                onSearchTriggered(searchQuery)
                                onTopicClick(topicId)
                            },
                            onFollowButtonClick = { onFollowButtonClick(topicId, it) },
                        )
                    }
                }
            }

            // 显示新闻资源列表
            if (newsResources.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = searchR.string.feature_search_updates))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // 新闻资源展示方式
                newsFeed(
                    feedState = Success(feed = newsResources),
                    onNewsResourcesCheckedChanged = onNewsResourcesCheckedChanged,
                    onNewsResourceViewed = onNewsResourceViewed,
                    onTopicClick = onTopicClick,
                    onExpandedCardClick = {
                        onSearchTriggered(searchQuery)
                    },
                )
            }
        }
        // 垂直方向添加快速滚动条
        val itemsAvailable = topics.size + newsResources.size
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

@Composable
private fun RecentSearchesBody(
    recentSearchQueries: List<String>,
    onClearRecentSearches: () -> Unit,
    onRecentSearchClicked: (String) -> Unit,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = searchR.string.feature_search_recent_searches))
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (recentSearchQueries.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClearRecentSearches()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector = NiaIcons.Close,
                        contentDescription = stringResource(
                            id = searchR.string.feature_search_clear_recent_searches_content_desc,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(recentSearchQueries) { recentSearch ->
                Text(
                    text = recentSearch,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .clickable { onRecentSearchClicked(recentSearch) }
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SearchToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        // 返回按钮
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = NiaIcons.ArrowBack,
                contentDescription = stringResource(
                    id = string.core_ui_back,
                ),
            )
        }
        // 查询文本输入框
        SearchTextField(
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
) {
    // 焦点请求
    val focusRequester = remember { FocusRequester() }
    // 本地软键盘
    val keyboardController = LocalSoftwareKeyboardController.current

    // 显示触发查询，保存查询条件，同时关闭软键盘
    val onSearchExplicitlyTriggered = {
        keyboardController?.hide()
        onSearchTriggered(searchQuery)
    }

    // 文本输入框
    TextField(
        // 输入框色调
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        // 输入框开始查询图标
        leadingIcon = {
            Icon(
                imageVector = NiaIcons.Search,
                contentDescription = stringResource(
                    id = searchR.string.feature_search_title,
                ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        // 输入框尾部的删除图标
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChanged("")
                    },
                ) {
                    Icon(
                        imageVector = NiaIcons.Close,
                        contentDescription = stringResource(
                            id = searchR.string.feature_search_clear_search_text_content_desc,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        onValueChange = {
            // 当输入改变时会触发查询条件改变
            if ("\n" !in it) onSearchQueryChanged(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            // 给输入框绑定焦点
            .focusRequester(focusRequester)
            // 绑定键盘回车事件
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    // 显示触发查询
                    onSearchExplicitlyTriggered()
                    true
                } else {
                    false
                }
            }
            .testTag("searchTextField"),
        shape = RoundedCornerShape(32.dp),
        value = searchQuery,
        // 软键盘右下角显示探索按钮
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        // 搜索按钮事件为触发查询事件
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchExplicitlyTriggered()
            },
        ),
        maxLines = 1,
        singleLine = true,
    )
    LaunchedEffect(Unit) {
        // 组件显示时输入框加焦点
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun SearchToolbarPreview() {
    NiaTheme {
        SearchToolbar(
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChanged = {},
            onSearchTriggered = {},
        )
    }
}

@Preview
@Composable
private fun EmptySearchResultColumnPreview() {
    NiaTheme {
        EmptySearchResultBody(
            onInterestsClick = {},
            searchQuery = "C++",
        )
    }
}

@Preview
@Composable
private fun RecentSearchesBodyPreview() {
    NiaTheme {
        RecentSearchesBody(
            onClearRecentSearches = {},
            onRecentSearchClicked = {},
            recentSearchQueries = listOf("kotlin", "jetpack compose", "testing"),
        )
    }
}

@Preview
@Composable
private fun SearchNotReadyBodyPreview() {
    NiaTheme {
        SearchNotReadyBody()
    }
}

@DevicePreviews
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchUiStatePreviewParameterProvider::class)
    searchResultUiState: SearchResultUiState,
) {
    NiaTheme {
        SearchScreen(searchResultUiState = searchResultUiState)
    }
}
