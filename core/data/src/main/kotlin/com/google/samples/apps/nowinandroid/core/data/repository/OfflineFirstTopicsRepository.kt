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

package com.google.samples.apps.nowinandroid.core.data.repository

import com.google.samples.apps.nowinandroid.core.data.Synchronizer
import com.google.samples.apps.nowinandroid.core.data.changeListSync
import com.google.samples.apps.nowinandroid.core.data.model.asEntity
import com.google.samples.apps.nowinandroid.core.database.dao.TopicDao
import com.google.samples.apps.nowinandroid.core.database.model.TopicEntity
import com.google.samples.apps.nowinandroid.core.database.model.asExternalModel
import com.google.samples.apps.nowinandroid.core.datastore.ChangeListVersions
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.network.NiaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Disk storage backed implementation of the [TopicsRepository].
 * Reads are exclusively from local storage to support offline access.
 * 离线主题仓库，本地数据库优先，声明为模块内部类
 * 依赖注入：操作本地数据库的Dao及后台API接口
 */
internal class OfflineFirstTopicsRepository @Inject constructor(
    // 依赖注入 Dao
    private val topicDao: TopicDao,
    // 依赖注入网络数据源
    private val network: NiaNetworkDataSource,
) : TopicsRepository {

    // 从本地读取数据，是Flow格式
    override fun getTopics(): Flow<List<Topic>> =

        topicDao.getTopicEntities()
            // 返回一个新流
            .map {
                // 读取流数据并进行转换，然后返回新流
                // 将表实体转为UI访问用到的领域模型
                //it.map(TopicEntity::asExternalModel)
                it.map { it.asExternalModel() }
            }

    override fun getTopic(id: String): Flow<Topic> =
        topicDao.getTopicEntity(id).map { it.asExternalModel() }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        // 调用通用的同步管理器模板方法

        synchronizer.changeListSync(
            // 1.回调函数：获取当前版本号
            // 引用属性topicVersion的getter方法
            // 读取本地当前版本号
            // ChangeListVersions 只是数据类定义，如何实现读取数据呢？
            // versionReader=ChangeListVersions::topicVersion，实际是创建了函数引用，
            // （即 (ChangeListVersions) -> Int，入参数是一个函数返回值是整数）
            // versionReader被赋值为 ChangeListVersions实例的topicVersion属性的getter方法
            versionReader = ChangeListVersions::topicVersion,
            // 2.回调函数：网络获取变化列表
            // 定义获取变化列表数据回调函数
            // 注意这里回调函数的声明方式：currentVersion是入参 ->后面是函数体
            changeListFetcher = { currentVersion ->
                // Lambda表达式如果没有显示调用return返回，那么最后一行代码将会作为返回值返回
                network.getTopicChangeList(after = currentVersion)
            },
            // 3.回调函数：更新topic版本号到本地
            // 本地版本号更新回调函数
            versionUpdater = { latestVersion ->
                copy(topicVersion = latestVersion)
            },
            // 4.回调函数：本地数据库删除主题的回调方法
            modelDeleter = topicDao::deleteTopics,
            // 5.回调函数：主题添加或更新
            modelUpdater = { changedIds ->
                val networkTopics = network.getTopics(ids = changedIds)
                topicDao.upsertTopics(
                    entities = networkTopics.map(NetworkTopic::asEntity),
                )
            },
        )
}
