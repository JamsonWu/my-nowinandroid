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

package com.google.samples.apps.nowinandroid.feature.interests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.feature.interests.navigation.TOPIC_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val userDataRepository: UserDataRepository,
    getFollowableTopics: GetFollowableTopicsUseCase,
) : ViewModel() {

    // var selectedTopicId = savedStateHandle.get<String>(TOPIC_ID_ARG)
    // 读取路由参数方法，路由跳转时相关参数值会存到savedStateHandle对象中
    // 这里为什么要用StateFlow方式读取被选中的主题Id呢？
    // 当savedStateHandle中改变TOPIC_ID_ARG的值时uiState能监听到流的变化
    val selectedTopicId: StateFlow<String?> = savedStateHandle.getStateFlow(TOPIC_ID_ARG, null)

    // 合并两个流数据，指定目标类型
    val uiState: StateFlow<InterestsUiState> = combine(
        // StateFlow 字符串流
        // 注意合并流时加这个ID字段的作用
        selectedTopicId,
        // 获取可关注的主题列表，这是一个奇特的类方法，按名称排序
        getFollowableTopics(sortBy = TopicSortField.NAME),
        //  流中数据转为目标类型
        InterestsUiState::Interests,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InterestsUiState.Loading,
    )

    // 关注主题
    fun followTopic(followedTopicId: String, followed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTopicIdFollowed(followedTopicId, followed)
        }
    }

    // 选中某个主题
    fun onTopicClick(topicId: String?) {
        savedStateHandle[TOPIC_ID_ARG] = topicId
    }
}

sealed interface InterestsUiState {
    data object Loading : InterestsUiState

    data class Interests(
        val selectedTopicId: String?,
        val topics: List<FollowableTopic>,
    ) : InterestsUiState

    data object Empty : InterestsUiState
}
