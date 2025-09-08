package com.example.photo_uploader_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.photo_uploader_app.data.data.database.Converters

@Database(entities = [PhotoEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
internal abstract class AppDb : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}
