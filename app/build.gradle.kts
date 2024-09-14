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
import com.google.samples.apps.nowinandroid.NiaBuildType

plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.application.flavors)
    alias(libs.plugins.nowinandroid.android.application.jacoco)
    alias(libs.plugins.nowinandroid.android.application.firebase)
    alias(libs.plugins.nowinandroid.hilt)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    // 管理应用程序的敏感信息，会将secrets指定的属性配置文件加入到BuildConfig对象中
     id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    // 默认配置都是预定义参数
    defaultConfig {
        // 应用包名，用于标识应用,BuildConfig 会包含这个字段
        applicationId = "com.google.samples.apps.nowinandroid"
        // 应用版本，内部更新使用
        versionCode = 8
        // 应用版本名称
        versionName = "0.1.2" // X.Y.Z; X = Major, Y = minor, Z = Patch level

        // Custom test runner to set up Hilt dependency graph
        // 自定义测试运行类
        testInstrumentationRunner = "com.google.samples.apps.nowinandroid.core.testing.NiaTestRunner"
        vectorDrawables {
            // 矢量图形兼容低版本
            useSupportLibrary = true
        }
    }
    buildFeatures {
        // 是否开启生成 BuildConfig 配置类
        buildConfig = true
        // 其中DEBUG,BUILD_TYPE,FLAVOR是根据编译相关配置自动加入的
        // LOG_ROOM_SQL 是使用buildConfigField手动添加的自定义项
        //public final class BuildConfig {
        //    public static final boolean DEBUG = Boolean.parseBoolean("true");
        //    public static final String APPLICATION_ID = "com.google.samples.apps.nowinandroid.demo.debug";
        //    public static final String BUILD_TYPE = "debug";
        //    public static final String FLAVOR = "demo";
        //    public static final int VERSION_CODE = 8;
        //    public static final String VERSION_NAME = "0.1.2";
        //    // Field from build type: debug
        //    public static final boolean LOG_ROOM_SQL = true;
        //}
    }

    buildTypes {
        // 调试
        debug {
            // 添加构建类型后缀
            applicationIdSuffix = NiaBuildType.DEBUG.applicationIdSuffix
            // 添加自定义构建字段配置
            buildConfigField( "boolean", "LOG_ROOM_SQL", "true")
        }
        // 发布
        release {
            // 启用代码混淆
            isMinifyEnabled = true
            // 添加构建类型后缀
            applicationIdSuffix = NiaBuildType.RELEASE.applicationIdSuffix
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // To publish on the Play store a private signing key is required, but to allow anyone
            // who clones the code to sign and run the release variant, use the debug signing key.
            // TODO: Abstract the signing configuration to a separate file to avoid hardcoding this.
            // 签名配置
            signingConfig = signingConfigs.named("debug").get()
            // Ensure Baseline Profile is fresh for release builds.
            // 在构建时自动生成基准配置文件
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }

    packaging {
        resources {
            // 构建时排除些不需要的资源文件
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    testOptions {
        unitTests {
            // 单元测试时包含android资源
            isIncludeAndroidResources = true
        }
    }
    // 指定命名空间
    namespace = "com.google.samples.apps.nowinandroid"
}

secrets {
   defaultPropertiesFileName = "secrets.defaults.properties"
}
dependencies {
    implementation(projects.feature.interests)
    implementation(projects.feature.foryou)
    implementation(projects.feature.bookmarks)
    implementation(projects.feature.topic)
    implementation(projects.feature.search)
    implementation(projects.feature.settings)

    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.analytics)
    implementation(projects.sync.work)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.runtime.tracing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    // implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.window.core)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.kt)

    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.testManifest)
    debugImplementation(projects.uiTestHiltManifest)

    kspTest(libs.hilt.compiler)

    testImplementation(projects.core.dataTest)
    testImplementation(libs.hilt.android.testing)
    testImplementation(projects.sync.syncTest)

    testDemoImplementation(libs.robolectric)
    testDemoImplementation(libs.roborazzi)
    testDemoImplementation(projects.core.screenshotTesting)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.core.datastoreTest)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
    baselineProfile(projects.benchmarks)

}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}

dependencyGuard {
    configuration("prodReleaseRuntimeClasspath")
}
