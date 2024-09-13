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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.MainActivityUiState.Loading
import com.google.samples.apps.nowinandroid.MainActivityUiState.Success
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
// 让ViewModel支持依赖注入
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    // 用户数据存在Preference
    // 这是一个仓库需要依赖注入，需要在constuctor前加@Inject
    // 由于用户数据比较复杂，所以使用proto定义
    userDataRepository: UserDataRepository,
) : ViewModel() {
    // 将Flow数据流转为StateFlow
    // 使用 stateIn将 cold Flow转为hot Flow
    // UI可订阅这个StateFlow实现状态可观察
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        // 使用map对流数据进行一次转换
        Success(it)
    }.stateIn(
        scope = viewModelScope,
        // 只要有订阅方，会发射一次初始值到StateFlow中
        // 然后在数据读取成功后再发射一次流数据到StateFlow中
        initialValue = Loading,
        // 设置共享流什么时候开始与什么时候结束
        // 当第一个订阅者出现时即开启共享流
        // 当最后一个订阅者消失5s后停止共享流
        started = SharingStarted.WhileSubscribed(5_000),
    )
}
// 在接口内定义数据对象或数据类，在外部可以直接访问这个数据对象与数据类
// 这里的数据对象Loading是单例对象，只有一个全局对象，不需要手动去实例化，所以使用时只要用Loading，而不是Loading()
// 在密封类sealed的情况下，所有子类都必须在父类内部定义
// MainActivityUiState接口定义了两个子类
// 在Kotlin中可以在接口内部定义类，包括数据对象与数据类
// 由于Loading与Success是数据类，所以直接直接访问
sealed interface MainActivityUiState {
    // 数据单例对象实现接口
    data object Loading : MainActivityUiState
    // 数据类实现接口
    data class Success(val userData: UserData) : MainActivityUiState
}
