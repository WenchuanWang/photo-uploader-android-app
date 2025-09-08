package com.example.photo_uploader_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.photo_uploader_app.core.model.UploadStatus

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localPath: String,
    val mimeType: String,
    val status: UploadStatus = UploadStatus.QUEUED,
    val remoteUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
