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

package com.google.samples.apps.nowinandroid.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import kotlinx.datetime.Instant

/**
 * Defines an NiA news resource.
 * 实体定义
 */
@Entity(
    // 数据库表名
    tableName = "news_resources",
)
data class NewsResourceEntity(
    // 声明id为主键
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    // 字段映射：name是表字段名，headerImageUrl是实体字段名
    @ColumnInfo(name = "header_image_url")
    val headerImageUrl: String?,
    @ColumnInfo(name = "publish_date")
    // Instant是日期时间类型，适合跨时区的日期时间操作
    val publishDate: Instant,
    val type: String,
)

// 实体扩展方法，作用是将实体转为领域模型
fun NewsResourceEntity.asExternalModel() = NewsResource(
    id = id,
    title = title,
    content = content,
    url = url,
    headerImageUrl = headerImageUrl,
    publishDate = publishDate,
    type = type,
    // 空列表
    topics = listOf(),
)
