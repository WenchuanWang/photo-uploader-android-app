package com.example.photo_uploader_app.domain.usecases

import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.core.usecase.GetAllPhotosUseCase
import com.example.photo_uploader_app.domain.model.Photo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class GetAllPhotosUseCaseTest {

    @Test
    fun `invoke returns repository flow`() = runTest {
        val repository: PhotoRepository = mockk()
        val useCase = GetAllPhotosUseCase(repository)
        val photoList = listOf(Photo(1, "/a.jpg", "image/jpeg"))
        val flow: Flow<List<Photo>> = flowOf(photoList)
        every { repository.getAllPhotos() } returns flow

        val result = useCase()

        Assert.assertEquals(flow, result)
    }
}