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

package com.google.samples.apps.nowinandroid.core.database

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.annotation.processing.Generated

/**
 * Automatic schema migrations sometimes require extra instructions to perform the migration, for
 * example, when a column is renamed. These extra instructions are placed here by creating a class
 * using the following naming convention `SchemaXtoY` where X is the schema version you're migrating
 * from and Y is the schema version you're migrating to. The class should implement
 * `AutoMigrationSpec`.
 * 数据库表自动升级配置
 * https://developer.android.com/reference/kotlin/androidx/room/AutoMigration
 */
internal object DatabaseMigrations {

    // 表字段改名
    @RenameColumn(
        tableName = "topics",
        fromColumnName = "description",
        toColumnName = "shortDescription",
    )
    // 自定义元数据升级，从第2版本升级到第3版本，自定义升级
    // 在插件AndroidRoomConventionPlugin中已定义元文件位置
    // extensions.configure<RoomExtension> {
    //    schemaDirectory("$projectDir/schemas")
    // }
    // An auto migration must define the 'from' and 'to' versions of the schema
    // Room automatically detects changes on the database between these two schemas,
    // and constructs a androidx.room.migration.Migration to migrate between the two versions
    // 根据From与To2个版本的元文件生成迁移脚本
    // 以下是生成代码示例
    //@Generated(value = ["androidx.room.RoomProcessor"])
    //@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
    //internal class NiaDatabase_AutoMigration_2_3_Impl : Migration {
    //    private val callback: AutoMigrationSpec = DatabaseMigrations.Schema2to3()
    //
    //    public constructor() : super(2, 3)
    //
    //    public override fun migrate(db: SupportSQLiteDatabase) {
    //        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_topics` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `shortDescription` TEXT NOT NULL, `longDescription` TEXT NOT NULL DEFAULT '', `url` TEXT NOT NULL DEFAULT '', `imageUrl` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
    //        db.execSQL("INSERT INTO `_new_topics` (`id`,`name`,`shortDescription`) SELECT `id`,`name`,`description` FROM `topics`")
    //        db.execSQL("DROP TABLE `topics`")
    //        db.execSQL("ALTER TABLE `_new_topics` RENAME TO `topics`")
    //        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_topics_name` ON `topics` (`name`)")
    //        callback.onPostMigrate(db)
    //    }
    //}
    class Schema2to3 : AutoMigrationSpec

    // 列删除
    @DeleteColumn(
        tableName = "news_resources",
        columnName = "episode_id",
    )
    // 删除表实体
    @DeleteTable.Entries(
        DeleteTable(
            tableName = "episodes_authors",
        ),
        DeleteTable(
            tableName = "episodes",
        ),
    )
    class Schema10to11 : AutoMigrationSpec

    // 删除表实体
    @DeleteTable.Entries(
        DeleteTable(
            tableName = "news_resources_authors",
        ),
        DeleteTable(
            tableName = "authors",
        ),
    )
    class Schema11to12 : AutoMigrationSpec
}
