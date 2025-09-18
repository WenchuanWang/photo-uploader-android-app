package com.example.photo_uploader_app.core.usecase

import android.net.Uri
import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.domain_api.IoDispatcher
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class InsertPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) {
    suspend operator fun invoke(uris: List<Uri>) = withContext(io) {
        if (uris.isEmpty()) return@withContext
        repository.insertPhotos(uris)
    }
}
