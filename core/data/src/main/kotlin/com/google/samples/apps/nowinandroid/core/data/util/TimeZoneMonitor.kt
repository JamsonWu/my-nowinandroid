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

package com.google.samples.apps.nowinandroid.core.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
import com.google.samples.apps.nowinandroid.core.network.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinTimeZone
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for reporting current timezone the device has set.
 * It always emits at least once with default setting and then for each TZ change.
 * 每一次时区设置改变都会广播通知至少一次
 */
interface TimeZoneMonitor {
    val currentTimeZone: Flow<TimeZone>
}

@Singleton
internal class TimeZoneBroadcastMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope appScope: CoroutineScope,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : TimeZoneMonitor {

    override val currentTimeZone: SharedFlow<TimeZone> =
        // 使用callbackFlow创建一个流Flow
        callbackFlow {
            // Send the default time zone first.
            // 向流发射一个值
            trySend(TimeZone.currentSystemDefault())

            // Registers BroadcastReceiver for the TimeZone changes
            // 注意这里的object作用是创建匿名类实例
            // 继承BroadcastReceiver创建匿名类实例
            val receiver = object : BroadcastReceiver() {
                // 广播接收处理
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

                    val zoneIdFromIntent = if (VERSION.SDK_INT < VERSION_CODES.R) {
                        null
                    } else {
                        // Starting Android R we also get the new TimeZone.
                        // 非空对象，然后再调用 let 代码块
                        intent.getStringExtra(Intent.EXTRA_TIMEZONE)?.let { timeZoneId ->
                            // We need to convert it from java.util.Timezone to java.time.ZoneId
                            val zoneId = ZoneId.of(timeZoneId, ZoneId.SHORT_IDS)
                            // Convert to kotlinx.datetime.TimeZone
                            zoneId.toKotlinTimeZone()
                        }
                    }

                    // If there isn't a zoneId in the intent, fallback to the systemDefault, which should also reflect the change
                    trySend(zoneIdFromIntent ?: TimeZone.currentSystemDefault())
                }
            }

            trace("TimeZoneBroadcastReceiver.register") {
                // 注册接收广播
                context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
            }

            // Send here again, because registering the Broadcast Receiver can take up to several milliseconds.
            // This way, we can reduce the likelihood that a TZ change wouldn't be caught with the Broadcast Receiver.
            trySend(TimeZone.currentSystemDefault())

            // 协程关闭或取消时会触发这个代码块
            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
            // We use to prevent multiple emissions of the same type, because we use trySend multiple times.
            // 由于多次发射值，要阻止多次发射相同类型的值
            .distinctUntilChanged()
            // 获取到最新数据值
            .conflate()
            // 确保流所有异步操作都在ioDispatcher执行
            // 指定流操作调度器，指定用什么线程池
            .flowOn(ioDispatcher)
            // Sharing the callback to prevent multiple BroadcastReceivers being registered
            // 共享流
            .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), 1)
}
