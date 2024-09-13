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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.result.asResult
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 收藏ViewModel定义
@HiltViewModel
// 使用注解 @Inject 实现构造函数依赖注入
class BookmarksViewModel @Inject constructor(
    // 用户数据仓库注入，UserDataRepository只是一个自定义接口，接口定义一个流数据及其它异步函数
    private val userDataRepository: UserDataRepository,
    // 注意这里没有定义为val也没加private
    // 那么这个常量只能在构造函数代码块内执行，在类内部的函数是无法调用的
    // 构造函数代码块是指这个类初始化过程执行的代码块，即类的{}代码块区域
    // 这种定义方式实际上只是为了限制作用域范围，这个依赖注入项并不是哪里都可以访问的
    userNewsResourceRepository: UserNewsResourceRepository,
) : ViewModel() {

    var shouldDisplayUndoBookmark by mutableStateOf(false)
    private var lastRemovedBookmarkId: String? = null

    val feedUiState: StateFlow<NewsFeedUiState> =
        // 当收藏资源取消后会自动重新收集最新值
        userNewsResourceRepository.observeAllBookmarked()
            // 使用map函数转换流数据：将List<UserNewsResource>类型数据转灾NewsFeedUiState类型
            // 传入流数据transform函数，将流原数据传给Success函数
            // 为了解流的加载过程，需要加入加载中状态，所以要对流原数据进行包装转换
            // 让UI收集数据时能知道加载进度状态
            .map<List<UserNewsResource>, NewsFeedUiState>(NewsFeedUiState::Success)
            // 开始进行流收集前向流发射一个值，告诉准备开始收集流了
            .onStart { emit(Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                //  提供初始值
                initialValue = Loading,
            )

    // 删除保存的收藏资源
    fun removeFromSavedResources(newsResourceId: String) {
        // 异步函数需要在协程上下文中执行
        viewModelScope.launch {
            shouldDisplayUndoBookmark = true
            lastRemovedBookmarkId = newsResourceId
            userDataRepository.setNewsResourceBookmarked(newsResourceId, false)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun undoBookmarkRemoval() {
        viewModelScope.launch {
            // 当对象非空时执行代码块内容
            lastRemovedBookmarkId?.let {
                userDataRepository.setNewsResourceBookmarked(it, true)
            }

        }
        clearUndoState()
    }

    fun clearUndoState() {
        shouldDisplayUndoBookmark = false
        lastRemovedBookmarkId = null
    }
}
