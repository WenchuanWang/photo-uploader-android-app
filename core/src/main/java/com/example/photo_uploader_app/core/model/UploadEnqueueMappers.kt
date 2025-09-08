package com.example.photo_uploader_app.core.model

import com.example.photo_uploader_app.core.util.extFromMime
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.domain_api.UploadEnqueueData

internal fun Photo.toUploadEnqueueData() = UploadEnqueueData(
    dedupKey = "photo_$id",
    localPath = localPath,
    mimeType = mimeType,
    ext = mimeType.extFromMime()
)
