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

package com.google.samples.apps.nowinandroid.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
// 定义接口:out T 确保了 Result 的子类可以持有 T 的子类型
// out T 是协变泛型参数
// 这意味着任何 Result 的子类都可以返回 T 的子类型
// 使用sealed定义类层次结构，可定义子类型集合，实现多态行为
// 那么模式匹配时就可以使用when表达式
// sealed常用于创建枚举类型
sealed interface Result<out T> {
    // 数据类实现接口，这种写法的作用是 Success is Result，方便判断
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}
// 在异步数据流中读取包装
// 转换流对象，将T流转换为Result<T>流
// map<T,Result<T>>是将T对象转换为Result<T>对象
fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>> { Result.Success(it) }
    // 开始流收集
    .onStart {
        // emit将数据发送到Flow流中
        // 如果Result不加out，那么会报错类型不匹配
        // Type mismatch.
        // Required: Result<T>
        // Found: Result.Loading
        emit(Result.Loading)
    }
    // 流收集时遇到错误
    .catch { emit(Result.Error(it)) }

fun main(){

}