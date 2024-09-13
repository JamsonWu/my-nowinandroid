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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.samples.apps.nowinandroid.feature.interests.navigation.TOPIC_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class Interests2PaneViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // 本来savedStateHandle[TOPIC_ID_ARG]就可以拿到值，为什么要转为StateFlow呢？
    // 通过转为StateFlow，那么selectedTopicId变成了可观察的状态了
    // 只要Flow中的值发生变化，那么流就会发生变化，从而让UI组件能监听到，然后重新渲染组件
    val selectedTopicId: StateFlow<String?> =
        // 当值发生变化后会重新返回一个新的StateFlow
        savedStateHandle.getStateFlow(TOPIC_ID_ARG, savedStateHandle[TOPIC_ID_ARG])

    fun onTopicClick(topicId: String?) {
        savedStateHandle[TOPIC_ID_ARG] = topicId
    }
}
