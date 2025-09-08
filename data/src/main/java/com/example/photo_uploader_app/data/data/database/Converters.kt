package com.example.photo_uploader_app.data.data.database

import androidx.room.TypeConverter
import com.example.photo_uploader_app.core.model.UploadStatus

internal class Converters {
    @TypeConverter
    fun fromStatus(value: UploadStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): UploadStatus = enumValueOf(value)
}
