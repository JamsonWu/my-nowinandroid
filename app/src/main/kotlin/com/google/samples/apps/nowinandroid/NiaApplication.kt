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

package com.google.samples.apps.nowinandroid

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.samples.apps.nowinandroid.sync.initializers.Sync
import com.google.samples.apps.nowinandroid.util.ProfileVerifierLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * [Application] class for NiA
 * Context applicationContext 就是 NiaApplication 的实例
 * 这个Context会注入到hilt框架中，那么在依赖注入时就可以直接使用@ApplicationContext注解即可直接注入
 */
@HiltAndroidApp
class NiaApplication : Application(), ImageLoaderFactory {
    // 图片加载器imageLoader依赖注入，在NetWorkModule提供依赖项
    // 注入的图片加载器是用于加载Svg图片的
    // 使用dagger.Lazy<T>可以延迟实例化直至首次调用get方法才进行实例化
    // 这么做的目的是对象仅当需要时才创建
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var profileVerifierLogger: ProfileVerifierLogger

    override fun onCreate() {
        super.onCreate()
        // Initialize Sync; the system responsible for keeping data in the app up to date.
        // 发起数据同步
        Sync.initialize(context = this)
        profileVerifierLogger()
    }

    // 调用newImageLoader()来获取一个图片加载器
    // 当调用newImageLoader来创建实例时才会调用imageLoader的get方法来真正进行实例化
    // 在哪里调用呢？在Coil单例对象newImageLoader方法中调用
    // Coil是什么？Coil是Android高性能图像加载库，支持协程与Composable等
    // @Synchronized
    // private fun newImageLoader(context: Context): ImageLoader {
    //    // Check again in case imageLoader was just set.
    //    Coil.imageLoader?.let { return it }
    //    // Create a new ImageLoader.
    //    val newImageLoader = imageLoaderFactory?.newImageLoader()
    //        ?: (context.applicationContext as? ImageLoaderFactory)?.newImageLoader()
    //        ?: ImageLoader(context)
    //    imageLoaderFactory = null
    //    Coil.imageLoader = newImageLoader
    //    return newImageLoader
    // }
    override fun newImageLoader(): ImageLoader {
        return imageLoader.get()
    }

}
