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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.data.repository.NewsResourceQuery
import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.result.Result
import com.google.samples.apps.nowinandroid.core.result.asResult
import com.google.samples.apps.nowinandroid.feature.topic.navigation.TopicArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    topicsRepository: TopicsRepository,
    userNewsResourceRepository: UserNewsResourceRepository,
) : ViewModel() {

    // 从导航栈中读取路由参数
    private val topicArgs: TopicArgs = TopicArgs(savedStateHandle)

    // 获取主题ID
    val topicId = topicArgs.topicId

    // 获取当前主题信息+是否已关注状态
    val topicUiState: StateFlow<TopicUiState> = topicUiState(
        topicId = topicArgs.topicId,
        userDataRepository = userDataRepository,
        topicsRepository = topicsRepository,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TopicUiState.Loading,
        )

    // 获取当前主题下所有新闻资源
    val newsUiState: StateFlow<NewsUiState> = newsUiState(
        topicId = topicArgs.topicId,
        userDataRepository = userDataRepository,
        userNewsResourceRepository = userNewsResourceRepository,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsUiState.Loading,
        )

    // 设置当前主题是否关注，followed=true是关注false是取消关注
    fun followTopicToggle(followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTopicIdFollowed(topicArgs.topicId, followed)
        }
    }

    // 设置是否收藏某个资源
    fun bookmarkNews(newsResourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, bookmarked)
        }
    }

    //  设置新闻资源是否浏览过
    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }
}

// 获主题详细信息+是否关注
private fun topicUiState(
    topicId: String,
    userDataRepository: UserDataRepository,
    topicsRepository: TopicsRepository,
): Flow<TopicUiState> {
    // Observe the followed topics, as they could change over time.
    val followedTopicIds: Flow<Set<String>> =
        // UserData数据流
        userDataRepository.userData
            // 用户数据流转转为字符串
            .map { it.followedTopics }

    /* Observe topic information
       获取某个主题内容
    * */

    val topicStream: Flow<Topic> = topicsRepository.getTopic(
        id = topicId,
    )

    // 合并两个流的目的是给当前主题流增加是否已关注的标识
    return combine(
        // 用户关注的主题列表
        followedTopicIds,
        // 当前主题内容
        topicStream,
        // 将两个Flow合并到Pair对象中，用::指向Pair函数引用
        ::Pair,
    )
        // Pair对象包含first与second两个字段
        // 包装流，将流转化为Result<T>对象，实际只是转换流，给流增加几种状态，Loading，Error 与 Success
        // 将流包装到Result对象中
        // 收集流过程增加一个加载中的状态
        .asResult()
        // 读取流然后进行转换返回新流
        .map { followedTopicToTopicResult ->
            when (followedTopicToTopicResult) {
                // 流转换成功
                is Result.Success -> {
                    val (followedTopics, topic) = followedTopicToTopicResult.data
                    TopicUiState.Success(
                        followableTopic = FollowableTopic(
                            topic = topic,
                            // 判断当前主题ID是否在关注列表中
                            // 用户判断当前主题是否在已关注主题列表中
                            isFollowed = topicId in followedTopics,
                        ),
                    )
                }
                // 流正在收集中
                is Result.Loading -> TopicUiState.Loading
                // 收集流失败（实际是转换流失败）
                is Result.Error -> TopicUiState.Error
                // else -> TopicUiState.Error
            }
        }
}

// 获取当前主题下的所有新闻资源列表
private fun newsUiState(
    topicId: String,
    userNewsResourceRepository: UserNewsResourceRepository,
    userDataRepository: UserDataRepository,
): Flow<NewsUiState> {
    // Observe news
    // 获取与当前主题下的用户新闻资源
    val newsStream: Flow<List<UserNewsResource>> = userNewsResourceRepository.observeAll(
        NewsResourceQuery(filterTopicIds = setOf(element = topicId)),
    )

    // Observe bookmarks
    // 用户收藏的新闻资源
    val bookmark: Flow<Set<String>> = userDataRepository.userData
        .map { it.bookmarkedNewsResources }

    return combine(newsStream, bookmark, ::Pair)
        .asResult()
        .map { newsToBookmarksResult ->
            when (newsToBookmarksResult) {
                // 这里只用了newStream数据，并没用到bookmark,所以这里的combine目前是没用的
                is Result.Success -> NewsUiState.Success(newsToBookmarksResult.data.first)
                is Result.Loading -> NewsUiState.Loading
                is Result.Error -> NewsUiState.Error
            }
        }
}

// 主题UI状态
sealed interface TopicUiState {
    data class Success(val followableTopic: FollowableTopic) : TopicUiState
    data object Error : TopicUiState
    data object Loading : TopicUiState
}

// 当前主题下的新闻资源残表
sealed interface NewsUiState {
    data class Success(val news: List<UserNewsResource>) : NewsUiState
    data object Error : NewsUiState
    data object Loading : NewsUiState
}
