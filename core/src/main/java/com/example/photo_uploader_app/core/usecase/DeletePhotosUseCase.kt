package com.example.photo_uploader_app.core.usecase

import com.example.photo_uploader_app.core.PhotoRepository
import javax.inject.Inject

class DeletePhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deletePhone(id)
    }
}
