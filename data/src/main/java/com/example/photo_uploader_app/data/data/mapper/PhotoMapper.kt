package com.example.photo_uploader_app.data.mapper

import com.example.photo_uploader_app.data.local.PhotoEntity
import com.example.photo_uploader_app.domain.model.Photo

internal fun PhotoEntity.toPhoto() = Photo(
    id = id,
    localPath = localPath,
    mimeType = mimeType,
    status = status
)
