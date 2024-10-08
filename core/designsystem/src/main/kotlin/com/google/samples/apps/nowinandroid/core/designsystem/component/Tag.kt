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

package com.google.samples.apps.nowinandroid.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme

/**
 * 定义主题标签组件
 */
@Composable
fun NiaTopicTag(
    modifier: Modifier = Modifier,
    // 是否已被关注
    followed: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        val containerColor = if (followed) {
            // 如果已关注，容器颜色为主容器色
            MaterialTheme.colorScheme.primaryContainer
        } else {
            // 如果未关注，容器色为surfaceVariant并设置透明度
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = NiaTagDefaults.UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA,
            )
        }
        // 使用文本按钮
        TextButton(
            onClick = onClick,
            enabled = enabled,
            // 设置颜色
            colors = ButtonDefaults.textButtonColors(
                // 设置容器颜色
                containerColor = containerColor,
                // 设置内容颜色，背景色为容器颜色
                contentColor = contentColorFor(backgroundColor = containerColor),
                // 禁用时容器颜色配置
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = NiaTagDefaults.DISABLED_TOPIC_TAG_CONTAINER_ALPHA,
                ),
            ),
        ) {
            // 设置文本的字体样式，设置LocalTextStyle样式，Text组件会使用这个样式
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                text()
            }
        }
    }
}

@ThemePreviews
@Composable
fun TagPreview() {
    NiaTheme {
        NiaTopicTag(followed = true, onClick = {}) {
            Text("Topic".uppercase())
        }
    }
}

/**
 * Now in Android tag default values.
 */
object NiaTagDefaults {
    const val UNFOLLOWED_TOPIC_TAG_CONTAINER_ALPHA = 0.5f

    // TODO: File bug
    // Button disabled container alpha value not exposed by ButtonDefaults
    const val DISABLED_TOPIC_TAG_CONTAINER_ALPHA = 0.12f
}
