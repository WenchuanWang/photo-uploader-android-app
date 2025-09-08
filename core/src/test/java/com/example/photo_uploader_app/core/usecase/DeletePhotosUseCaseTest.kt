package com.example.photo_uploader_app.domain.usecases

import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.core.usecase.DeletePhotosUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeletePhotosUseCaseTest {

    @Test
    fun `invoke calls repository deletePhone with id`() = runTest {
        val repository: PhotoRepository = mockk()
        val useCase = DeletePhotosUseCase(repository)
        coEvery { repository.deletePhone(10L) } returns Unit

        useCase(10L)

        coVerify(exactly = 1) { repository.deletePhone(10L) }
    }
}