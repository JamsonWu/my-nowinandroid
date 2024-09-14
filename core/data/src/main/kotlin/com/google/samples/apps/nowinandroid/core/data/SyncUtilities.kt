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

package com.google.samples.apps.nowinandroid.core.data

import android.util.Log
import com.google.samples.apps.nowinandroid.core.datastore.ChangeListVersions
import com.google.samples.apps.nowinandroid.core.network.model.NetworkChangeList
import kotlinx.coroutines.async
import kotlin.coroutines.cancellation.CancellationException

/**
 * Interface marker for a class that manages synchronization between local data and a remote
 * source for a [Syncable].
 */
interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions

    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)

    /**
     * 语法糖
     * Syntactic sugar to call [Syncable.syncWith] while omitting the synchronizer argument
     * 允许实现了接口 Syncable的对象直接调用 sync方法而不用传递参数 Synchronizer
     * this@sync是指调用Syncable.sync()扩展函数所在类的实例，而这个类是有syncWith函数，
     * 而syncWith是有一个入参，入参隐式传入
     * this@Synchronizer是sync函数调用时上下文中的Synchronizer实例
     */
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
}

/**
 * Interface marker for a class that is synchronized with a remote source. Syncing must not be
 * performed concurrently and it is the [Synchronizer]'s responsibility to ensure this.
 */
interface Syncable {
    /**
     * Synchronizes the local database backing the repository with the network.
     * Returns if the sync was successful or not.
     */
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

/**
 * 封装异步执行代码块，目的是异步处理
 * Attempts [block], returning a successful [Result] if it succeeds, otherwise a [Result.Failure]
 * taking care not to break structured concurrency
 */
private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Log.i(
        "suspendRunCatching",
        "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result",
        exception,
    )
    Result.failure(exception)
}

/**
 * 定义本地与远程数据同步流程，即抽象模板方法，在模板中体现了整个更新步骤
 * 差异化代码在子类实现，因为这个模板方法是通用模板，可提供不同的子类使用
 * 网络同步本地数据一个通用方法
 * Utility function for syncing a repository with the network.
 * 需要被同步的模型版本
 * [versionReader] Reads the current version of the model that needs to be synced
 * 获取模型变化列表
 * [changeListFetcher] Fetches the change list for the model
 * 版本更新
 * [versionUpdater] Updates the [ChangeListVersions] after a successful sync
 * 模型删除
 * [modelDeleter] Deletes models by consuming the ids of the models that have been deleted.
 * [modelUpdater] Updates models by consuming the ids of the models that have changed.
 *
 * Note that the blocks defined above are never run concurrently, and the [Synchronizer]
 * implementation must guarantee this.
 */
suspend fun Synchronizer.changeListSync(
    versionReader: (ChangeListVersions) -> Int,
    changeListFetcher: suspend (Int) -> List<NetworkChangeList>,
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    modelDeleter: suspend (List<String>) -> Unit,
    modelUpdater: suspend (List<String>) -> Unit,
) = suspendRunCatching {
    // 第一步：读取当前版本号
    // Fetch the change list since last sync (akin to a git fetch)
    // 获取当前版本，这个versionReader是属性的setter函数
    // getChangeListVersions是从本地的preference中读取当前版本号数据，这是个异步方法
    val currentVersion = versionReader(getChangeListVersions())
    // 第二步：根据当前版本号读取服务器变化的数据列表
    // 这是异步方法，但下面的代码却是同步逻辑
    // 原因是数据同步的逻辑代码是封装在 awaitAll( async{处理})内，实现并行同步处理代码
    // val syncedSuccessfully = kotlinx.coroutines.awaitAll(
    //    async { topicRepository.sync() },
    //    async { newsRepository.sync() },
    // ).all { it }
    // 调用接口返回服务器变化列表

    val changeList = changeListFetcher(currentVersion)
    // 如果返回变化列表为空，则从当前方法退出
    if (changeList.isEmpty()) return@suspendRunCatching true

    // 第三步：分离出服务器已删除的数据ID列表与已更新的数据ID列表
    // 被删除了 要求更新
    // 对changeList进行分隔，第1个字段是被删除列表，第2个字段是更
    // 对返回结果使用partition函数对数据进行分离，对于满足条件的数据放在Pair对象中的first字段上
    // 不足足条件的数据放在Pair对象上的second字段上
    // Pair就是一个存储两个字段的对象
    val (deleted, updated) = changeList.partition(NetworkChangeList::isDelete)

    // 第四步：从本地删除服务器中已删除的数据列表
    // Delete models that have been deleted server-side
    // 传参需要删除的ID列表
    // 接口返回已删除的列表，对deleted变化的对象列表分离出其id列表
    // 注意::是函数引用，但这里只是一个id字段，事实上是引用id属性字段所对应的getter函数
    modelDeleter(deleted.map(NetworkChangeList::id))
    // 第五步：根据服务器已更新的ID列表调用后台接口返回更新的数据列表并更新本地数据库

    // Using the change list, pull down and save the changes (akin to a git pull)
    // 传参需要更新的列表ID
    modelUpdater(updated.map(NetworkChangeList::id))

    // 第六步：获取变化列表最后一个版本号然后更新本地的版本号
    // Update the last synced version (akin to updating local git HEAD)
    // 获取变化列表最后一个版本号
    val latestVersion = changeList.last().changeListVersion
    // 更新变化列表版本号
    updateChangeListVersions {
        versionUpdater(latestVersion)
    }
}.isSuccess
