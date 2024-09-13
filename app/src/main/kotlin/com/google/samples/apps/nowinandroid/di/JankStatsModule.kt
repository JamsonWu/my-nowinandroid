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

package com.google.samples.apps.nowinandroid.di

import android.app.Activity
import android.util.Log
import android.view.Window
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.JankStats.OnFrameListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object JankStatsModule {
    @Provides
    fun providesOnFrameListener(): OnFrameListener = OnFrameListener { frameData ->
        // Make sure to only log janky frames.
        // 标识渲染时间过长的帧，跟踪渲染卡顿位置
        if (frameData.isJank) {
            // We're currently logging this but would better report it to a backend.
            Log.v("NiA Jank", frameData.toString())
        }
    }

    @Provides
    fun providesWindow(activity: Activity): Window = activity.window

    @Provides
    fun providesJankStats(
        // 实例化JankStats需要传入两个参数，一个是Activity空口，还有一个是Frame监听器
        // 在监听器中可以做日志处理，为window追踪Jank指标
        window: Window,
        frameListener: OnFrameListener,
    ): JankStats = JankStats.createAndTrack(window, frameListener)
}
