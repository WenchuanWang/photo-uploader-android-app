package com.example.photo_uploader_app.domain.model

import com.example.photo_uploader_app.core.model.UploadStatus

data class Photo(
    val id: Long,
    val localPath: String,
    val mimeType: String,
    val status: UploadStatus = UploadStatus.QUEUED
)
