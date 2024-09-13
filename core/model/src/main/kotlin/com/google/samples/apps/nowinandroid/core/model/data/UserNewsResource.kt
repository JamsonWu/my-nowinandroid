/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.model.data

import kotlinx.datetime.Instant

/**
 * A [NewsResource] with additional user information such as whether the user is following the
 * news resource's topics and whether they have saved (bookmarked) this news resource.
 */
// 加上internal说明只能在当前模块内才能访问
// 数据类主构造函数声明字段
data class UserNewsResource internal constructor(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val headerImageUrl: String?,
    val publishDate: Instant,
    val type: String,
    val followableTopics: List<FollowableTopic>,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {
    // 次构造函数目的是为了方便外部调用
    // this()是使用主构造函数来创建UserNewsResource实例
    constructor(newsResource: NewsResource, userData: UserData) : this(
        id = newsResource.id,
        title = newsResource.title,
        content = newsResource.content,
        url = newsResource.url,
        headerImageUrl = newsResource.headerImageUrl,
        publishDate = newsResource.publishDate,
        type = newsResource.type,
        followableTopics = newsResource.topics.map { topic ->
            FollowableTopic(
                topic = topic,
                // 语法点：判断集合中是否包含某个值
                isFollowed = topic.id in userData.followedTopics,
            )
        },
        isSaved = newsResource.id in userData.bookmarkedNewsResources,
        hasBeenViewed = newsResource.id in userData.viewedNewsResources,
    )
}

// 语法点：箭头表达式
fun List<NewsResource>.mapToUserNewsResources(userData: UserData): List<UserNewsResource> =
// 这里的it是调用mapToUserNewsResources这个函数的List<NewsResource>数据列表
    // 隐式传递参数
    // 这里{}代表是Lambda表达式的函数体
    map { UserNewsResource(it, userData) }

//语法点： 不用=改为原始写法
fun List<NewsResource>.mapToUserNewsResources2(userData: UserData): List<UserNewsResource> {
    return map { UserNewsResource(it, userData) }
}
//语法点：
fun List<NewsResource>.mapToUserNewsResources3(userData: UserData): List<UserNewsResource> {
    //
    return map {
        // 如果不用箭头函数，那么默认会将值传递给it
        newsResource ->
        UserNewsResource(newsResource, userData)
    }
}

// 语法注意点
//fun List<NewsResource>.mapToUserNewsResources4(userData: UserData): List<UserNewsResource> {
//    //                           箭头函数用{}会被识别为Lambda表达式函数，那么是返回一个函数，所以会报类型不切匹配错误
//    return map { newsResource -> {
//                                   val userNewsResource = UserNewsResource(newsResource, userData)
//                                   userNewsResource
//                                 }
//               }
//}
//语法点： 如果不用Lambda表达式，则要用如何语法改写
fun List<NewsResource>.mapToUserNewsResources5(userData: UserData): List<UserNewsResource> {
    val transformedList = mutableListOf<UserNewsResource>()
    // this代表List<NewsResource>对象本身
    for (newsResource in this) {
        transformedList.add(UserNewsResource(newsResource, userData))
    }
    return transformedList
}

