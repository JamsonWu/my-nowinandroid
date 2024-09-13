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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent.Param
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.repository.RecentSearchRepository
import com.google.samples.apps.nowinandroid.core.data.repository.SearchContentsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.domain.GetRecentSearchQueriesUseCase
import com.google.samples.apps.nowinandroid.core.domain.GetSearchContentsUseCase
import com.google.samples.apps.nowinandroid.core.model.data.UserSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    getSearchContentsUseCase: GetSearchContentsUseCase,
    recentSearchQueriesUseCase: GetRecentSearchQueriesUseCase,
    private val searchContentsRepository: SearchContentsRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    // 获取查询条件值
    val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    // 查询返回UI状态
    // 当searchQuery值发生改变时会自动触发流重新收集流
    val searchResultUiState: StateFlow<SearchResultUiState> =
        // 获取本地仓库所新闻资源数量+本地仓库主题数量
        searchContentsRepository.getSearchContentsCount()
            // 读取最新流数据
            .flatMapLatest { totalCount ->
                if (totalCount < SEARCH_MIN_FTS_ENTITY_COUNT) {
                    flowOf(SearchResultUiState.SearchNotReady)
                } else {
                    searchQuery.flatMapLatest { query ->
                        // 查询内容长度要>=SEARCH_QUERY_MIN_LENGTH
                        if (query.length < SEARCH_QUERY_MIN_LENGTH) {
                            flowOf(SearchResultUiState.EmptyQuery)
                        } else {
                            // 执行查询
                            getSearchContentsUseCase(query)
                                // Not using .asResult() here, because it emits Loading state every
                                // time the user types a letter in the search box, which flickers the screen.
                                // 查询结果转换为UI状态map<T,R>
                                .map<UserSearchResult, SearchResultUiState> { data ->
                                    SearchResultUiState.Success(
                                        topics = data.topics,
                                        newsResources = data.newsResources,
                                    )
                                }
                                // 如果流收集异常，则发送异步通知
                                .catch { emit(SearchResultUiState.LoadFailed) }
                        }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchResultUiState.Loading,
            )

    // 最近的查询条件UI状态
    val recentSearchQueriesUiState: StateFlow<RecentSearchQueriesUiState> =
        recentSearchQueriesUseCase()
            .map(RecentSearchQueriesUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RecentSearchQueriesUiState.Loading,
            )

    // 查询内容改变时触发事件，设置查询条件值
    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    /**
     * Called when the search action is explicitly triggered by the user. For example, when the
     * search icon is tapped in the IME or when the enter key is pressed in the search text field.
     *
     * The search results are displayed on the fly as the user types, but to explicitly save the
     * search query in the search text field, defining this method.
     * 保存最近的查询条件
     */
    fun onSearchTriggered(query: String) {
        viewModelScope.launch {
            recentSearchRepository.insertOrReplaceRecentSearch(searchQuery = query)
        }
        analyticsHelper.logEventSearchTriggered(query = query)
    }

    // 清除最近查询条件
    fun clearRecentSearches() {
        viewModelScope.launch {
            recentSearchRepository.clearRecentSearches()
        }
    }

    // 设置新闻收藏
    fun setNewsResourceBookmarked(newsResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, isChecked)
        }
    }

    // 设置主题关注
    fun followTopic(followedTopicId: String, followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTopicIdFollowed(followedTopicId, followed)
        }
    }

    // 设置新闻资源已读
    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }
}

private fun AnalyticsHelper.logEventSearchTriggered(query: String) =
    logEvent(
        event = AnalyticsEvent(
            type = SEARCH_QUERY,
            extras = listOf(element = Param(key = SEARCH_QUERY, value = query)),
        ),
    )

/** Minimum length where search query is considered as [SearchResultUiState.EmptyQuery] */
private const val SEARCH_QUERY_MIN_LENGTH = 2

/** Minimum number of the fts table's entity count where it's considered as search is not ready */
private const val SEARCH_MIN_FTS_ENTITY_COUNT = 1
private const val SEARCH_QUERY = "searchQuery"
