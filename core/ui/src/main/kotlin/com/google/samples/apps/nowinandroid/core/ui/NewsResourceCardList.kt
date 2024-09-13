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

package com.google.samples.apps.nowinandroid.core.ui

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.samples.apps.nowinandroid.core.analytics.LocalAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource

/**
 * Extension function for displaying a [List] of [NewsResourceCardExpanded] backed by a list of
 * [UserNewsResource]s.
 *
 * [onToggleBookmark] defines the action invoked when a user wishes to bookmark an item
 * When a news resource card is tapped it will open the news resource URL in a Chrome Custom Tab.
 */
fun LazyListScope.userNewsResourceCardItems(
    items: List<UserNewsResource>,
    onToggleBookmark: (item: UserNewsResource) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    itemModifier: Modifier = Modifier,
) = items(
    items = items,
    key = { it.id }) {
    // 遍历新闻列表
    userNewsResource ->
    val resourceUrl = Uri.parse(userNewsResource.url)
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
    // 拿个context这么简单啊
    val context = LocalContext.current
    val analyticsHelper = LocalAnalyticsHelper.current

    // 渲染每条新闻资源数据
    NewsResourceCardExpanded(
        userNewsResource = userNewsResource,
        // 新闻资源是否已收藏
        isBookmarked = userNewsResource.isSaved,
        // 新闻资源是否已读
        hasBeenViewed = userNewsResource.hasBeenViewed,
        // 收藏事件按钮
        onToggleBookmark = { onToggleBookmark(userNewsResource) },
        // 点击事件打开浏览器显示，并设置已读
        // 点击事件是整个卡片区域
        onClick = {
            analyticsHelper.logNewsResourceOpened(
                newsResourceId = userNewsResource.id,
            )
            launchCustomChromeTab(context, resourceUrl, backgroundColor)
            onNewsResourceViewed(userNewsResource.id)
        },
        // 主题点击事件
        onTopicClick = onTopicClick,
        modifier = itemModifier,
    )
}
