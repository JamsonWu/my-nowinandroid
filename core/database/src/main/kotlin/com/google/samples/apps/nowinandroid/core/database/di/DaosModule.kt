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

package com.google.samples.apps.nowinandroid.core.database.di

import com.google.samples.apps.nowinandroid.core.database.NiaDatabase
import com.google.samples.apps.nowinandroid.core.database.dao.NewsResourceDao
import com.google.samples.apps.nowinandroid.core.database.dao.NewsResourceFtsDao
import com.google.samples.apps.nowinandroid.core.database.dao.RecentSearchQueryDao
import com.google.samples.apps.nowinandroid.core.database.dao.TopicDao
import com.google.samples.apps.nowinandroid.core.database.dao.TopicFtsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// 提供操作数据库表的相关Dao
// 这些Dao需要去拿数据库中定义的抽象Dao
// 所以需要注入NiaDatabase实例

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    // database.topicDao()方法调用，返回TopicData实例，提供对表topic的操作
    // 注意Dao实例需要从到数据库实例中去获取
    @Provides
    fun providesTopicsDao(
        database: NiaDatabase,
    ): TopicDao = database.topicDao()

    @Provides
    fun providesNewsResourceDao(
        database: NiaDatabase,
    ): NewsResourceDao = database.newsResourceDao()

    @Provides
    fun providesTopicFtsDao(
        database: NiaDatabase,
    ): TopicFtsDao = database.topicFtsDao()

    @Provides
    fun providesNewsResourceFtsDao(
        database: NiaDatabase,
    ): NewsResourceFtsDao = database.newsResourceFtsDao()

    @Provides
    fun providesRecentSearchQueryDao(
        database: NiaDatabase,
    ): RecentSearchQueryDao = database.recentSearchQueryDao()
}
