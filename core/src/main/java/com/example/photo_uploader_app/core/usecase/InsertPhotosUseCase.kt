package com.example.photo_uploader_app.core.usecase

import android.net.Uri
import com.example.photo_uploader_app.core.PhotoRepository
import jakarta.inject.Inject

class InsertPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(uris: List<Uri>) {
        if (uris.isEmpty()) return
        repository.insertPhotos(uris)
    }
}
