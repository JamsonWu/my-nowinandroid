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

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme

/**
 * Now in Android filter chip with included leading checked icon as well as text content slot.
 * 图文组件，可点击选中，扩展FilterChip组件
 * @param selected Whether the chip is currently checked.
 * @param onSelectedChange Called when the user clicks the chip and toggles checked.
 * @param modifier Modifier to be applied to the chip.
 * @param enabled Controls the enabled state of the chip. When `false`, this chip will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The text label content.
 */
@Composable
fun NiaFilterChip(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            // 给 LocalTextStyle 赋值，改变 label 的字体样式
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                //
                label()
            }
        },
        modifier = modifier,
        enabled = enabled,
        // 当选中时左边显示选中图标
        leadingIcon = if (selected) {
            {
                Icon(
                    // Nia组件常用图标，是矢量图标
                    imageVector = NiaIcons.Check,
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        // 形状：圆角，方形等
        shape = CircleShape,
        // 设置边框
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.onBackground,
            selectedBorderColor = MaterialTheme.colorScheme.onBackground,
            disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = NiaChipDefaults.DISABLED_CHIP_CONTENT_ALPHA,
            ),
            disabledSelectedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                // 设置透明度 0.38f
                alpha = NiaChipDefaults.DISABLED_CHIP_CONTENT_ALPHA,
            ),
            selectedBorderWidth = NiaChipDefaults.ChipBorderWidth,
        ),
        colors = FilterChipDefaults.filterChipColors(
            // label与icon同色
            labelColor = MaterialTheme.colorScheme.onBackground,
            iconColor = MaterialTheme.colorScheme.onBackground,
            // 禁用时容器颜色，设置不透明度
            disabledContainerColor = if (selected) {
                MaterialTheme.colorScheme.onBackground.copy(
                    alpha = NiaChipDefaults.DISABLED_CHIP_CONTAINER_ALPHA,
                )
            } else {
                Color.Transparent
            },
            disabledLabelColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = NiaChipDefaults.DISABLED_CHIP_CONTENT_ALPHA,
            ),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = NiaChipDefaults.DISABLED_CHIP_CONTENT_ALPHA,
            ),
            // 选中容器颜色为主容器色
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            // label与图标是背景文字颜色
            selectedLabelColor = MaterialTheme.colorScheme.onBackground,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@ThemePreviews
@Composable
fun ChipPreview() {
    NiaTheme {
        NiaBackground(modifier = Modifier.size(100.dp, 30.dp)) {
            NiaFilterChip(selected = true, onSelectedChange = {}) {
                Text("checked")
            }
        }
    }
}

/**
 * Now in Android chip default values.
 */
object NiaChipDefaults {
    // TODO: File bug
    // FilterChip default values aren't exposed via FilterChipDefaults
    const val DISABLED_CHIP_CONTAINER_ALPHA = 0.12f
    const val DISABLED_CHIP_CONTENT_ALPHA = 0.38f
    val ChipBorderWidth = 1.dp
}
