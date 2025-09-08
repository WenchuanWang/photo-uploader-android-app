package com.example.photo_uploader_app.core.usecase

import android.net.Uri
import com.example.photo_uploader_app.core.DispatcherProvider
import com.example.photo_uploader_app.core.PhotoRepository
import jakarta.inject.Inject
import kotlinx.coroutines.withContext

class InsertPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(uris: List<Uri>) = withContext(dispatchers.io) {
        if (uris.isEmpty()) return@withContext
        repository.insertPhotos(uris)
    }
}
