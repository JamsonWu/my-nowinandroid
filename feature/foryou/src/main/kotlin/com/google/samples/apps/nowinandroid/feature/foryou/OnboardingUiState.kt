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

package com.google.samples.apps.nowinandroid.feature.foryou

import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic

/**
 * A sealed hierarchy describing the onboarding state for the for you screen.
 * 用密封接口定义所有可能状态：类型安全，简洁，还有一个重要特点是可扩展（可携带数据）
 * 这种定义适合UI状态管理
 * 如果不需要定义携带数据，那么用枚举类定义也可以 enum class
 */
sealed interface OnboardingUiState {
    /**
     * The onboarding state is loading.
     * 因无成员，所以只要使用object声明为单例对象
     */
    data object Loading : OnboardingUiState

    /**
     * The onboarding state was unable to load.
     */
    data object LoadFailed : OnboardingUiState

    /**
     * There is no onboarding state.
     */
    data object NotShown : OnboardingUiState

    /**
     * There is a onboarding state, with the given lists of topics.
     * 因为有成员所以需要使用class 声明数据类
     * 这是携带数据的状态
     */
    data class Shown(
        val topics: List<FollowableTopic>,
    ) : OnboardingUiState {
        /**
         * True if the onboarding can be dismissed.
         * any表示至少有一个条件满足断言，则返回true
         */
        val isDismissable: Boolean get() = topics.any { it.isFollowed }
    }
}
