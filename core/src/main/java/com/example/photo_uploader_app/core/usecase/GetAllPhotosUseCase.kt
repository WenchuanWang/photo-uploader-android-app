package com.example.photo_uploader_app.core.usecase

import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> = repository.getAllPhotos()
}
