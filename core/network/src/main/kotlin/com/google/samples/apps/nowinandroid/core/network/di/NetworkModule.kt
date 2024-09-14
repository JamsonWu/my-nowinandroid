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

package com.google.samples.apps.nowinandroid.core.network.di

import android.content.Context
import androidx.tracing.trace
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.google.samples.apps.nowinandroid.core.network.BuildConfig
import com.google.samples.apps.nowinandroid.core.network.demo.DemoAssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesDemoAssetManager(
        // @ApplicationContext是hilt框架提供的，加上这个注解
        // 实现依赖注入Context
        @ApplicationContext context: Context,
    ): DemoAssetManager = DemoAssetManager(context.assets::open)

    @Provides
    @Singleton
    // 创建网络请求工厂实例 Call.Factory，因为OkHttpClient也实现了接口Call.Factory
    fun okHttpCallFactory(): Call.Factory = trace("NiaOkHttpClient") {
        // http通讯
        OkHttpClient.Builder()
            // 1.添加日志拦截器
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            setLevel(HttpLoggingInterceptor.Level.BODY)
                        }
                    },
            )

            //  匿名类实现添加header的传统写法
            //    .addInterceptor(object : Interceptor {
            //        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            //            val originalRequest = chain.request()
            //            val newRequest = originalRequest.newBuilder()
            //                .header("Authorization", "Bearer YOUR_TOKEN")
            //                .header("Accept", "application/json")
            //                .build()
            //            return chain.proceed(newRequest)
            //        }
            //    })

            // 2.匿名类实现添加header的简便写法-Lambda表达式
            // 需要创建一个保存token的单例，
            .addInterceptor(
                Interceptor { chain ->
                    val original = chain.request()
                    // 创建一个新的请求，并添加自定义的Header
                    val newRequest = original.newBuilder()
                        .header("Authorization", "Bearer YOUR_TOKEN")
                        .build()
                    // 继续执行链中的下一个拦截器
                    chain.proceed(newRequest)
                }
            )

            .build()
    }

    /**
     * Since we're displaying SVGs in the app, Coil needs an ImageLoader which supports this
     * format. During Coil's initialization it will call `applicationContext.newImageLoader()` to
     * obtain an ImageLoader.
     * Coil是android图片加载库
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     */
    @Provides
    @Singleton
    fun imageLoader(
        // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger.
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context,
    ): ImageLoader = trace("NiaImageLoader") {
        ImageLoader.Builder(application)
            // ImageLoader 添加http请求实例
            // 设置网络请求工厂，传递http请求的实例
            .callFactory { okHttpCallFactory.get() }
            // ImageLoader添加Svg解码器
            // 解析Svg
            .components {
                // 添加Svg解码器工厂
                add(SvgDecoder.Factory())
                // 添加自定义的图片格式解码器工厂。。。
            }
            // 设置请求头缓存
            // Assume most content images are versioned urls
            // but some problematic images are fetching each time
            .respectCacheHeaders(false)
            // 这里使用 apply 的原因是 apply的 Lambda表达式要
            // 调用ImageLoader.Builder(application)实例内的logger方法
            // 从而实现了链式调用
            .apply {
                // 因为加了一个判断条件，所以才需要用到 apply
                // BuildConfig是哪来的？
                // BuildConfig.DEBUG在哪里配置的？
                if (BuildConfig.DEBUG) {
                    // logger函数是ImageLoader.Builder类提供供的
                    logger(DebugLogger())
                }
            }

            .build()
    }
}
