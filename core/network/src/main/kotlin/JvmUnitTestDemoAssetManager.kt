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

import android.util.Log
import com.google.samples.apps.nowinandroid.core.network.demo.DemoAssetManager
import java.io.File
import java.io.InputStream
import java.util.Properties

/**
 * This class helps with loading Android `/assets` files, especially when running JVM unit tests.
 * It must remain on the root package for an easier [Class.getResource] with relative paths.
 * @see <a href="https://developer.android.com/reference/tools/gradle-api/7.3/com/android/build/api/dsl/UnitTestOptions">UnitTestOptions</a>
 */

// 注意这里是使用object来声明单例对象
// 为什么可以用object呢？因为单例对象只是应用期间内只维护一个实例而已，其实是一个特殊的类
// 适用于测试场景
internal object JvmUnitTestDemoAssetManager : DemoAssetManager {
    // 获取资源文件
    // 使用这种方式可以动态获取，从而不需要写死代码
    private val config =
        requireNotNull(javaClass.getResource("com/android/tools/test_config.properties")) {
            """
            Missing Android resources properties file.
            Did you forget to enable the feature in the gradle build file?
            android.testOptions.unitTests.isIncludeAndroidResources = true
            """.trimIndent()
        }
    // 以下配置是启用包含 Android 资源的单元测试功能。
    //    android {
    //        testOptions {
    //            unitTests {
    //                isIncludeAndroidResources = true
    //            }
    //        }
    //    }
    // 加载属性文件
    private val properties = Properties().apply { config.openStream().use(::load) }
    // 获取assets目录
    private val assets = File(properties["android_merged_assets"].toString())

    override fun open(fileName: String): InputStream
    {
        Log.d("JvmUnitTestDemo", "open: ")
        return File(assets, fileName).inputStream()
    }
}
