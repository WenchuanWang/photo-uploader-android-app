package com.example.photo_uploader_app.core.usecase

import com.example.photo_uploader_app.domain_api.UploadScheduler
import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.core.model.toUploadEnqueueData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class UploadPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
    private val scheduler: UploadScheduler
) {
    suspend operator fun invoke(scope: CoroutineScope) {
        repository.getPendingUploadPhotos()
            .distinctUntilChanged()
            .onEach { photos ->
                photos.forEach { item ->
                    scheduler.enqueueUpload(item.toUploadEnqueueData())
                }
            }
            .launchIn(scope)
    }
}
