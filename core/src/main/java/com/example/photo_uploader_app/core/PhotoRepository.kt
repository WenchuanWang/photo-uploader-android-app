package com.example.photo_uploader_app.core

import android.net.Uri
import com.example.photo_uploader_app.domain.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getAllPhotos(): Flow<List<Photo>>
    suspend fun getPendingUploadPhotos(): Flow<List<Photo>>
    suspend fun insertPhotos(uris: List<Uri>)
    suspend fun deletePhone(id: Long)
}
