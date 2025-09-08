package com.example.photo_uploader_app.domain.usecases

import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.core.usecase.UploadPhotosUseCase
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.domain_api.UploadScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadPhotosUseCaseTest {
    private val repository: PhotoRepository = mockk()
    private val scheduler: UploadScheduler = mockk(relaxed = true)
    private val appScope = CoroutineScope(UnconfinedTestDispatcher())
    private val useCase = UploadPhotosUseCase(repository, scheduler)

    @Test
    fun `invoke subscribes and enqueues ids`() = runTest {
        val flow = MutableSharedFlow<List<Photo>>(replay = 0)
        every { repository.getPendingUploadPhotos() } returns flow

        useCase(appScope)

        val photos = listOf(
            Photo(1, "/a", "image/jpeg"),
            Photo(2, "/b", "image/jpeg"),
            Photo(3, "/c", "image/jpeg")
        )
        flow.emit(photos)

        verify(exactly = 3) { scheduler.enqueueUpload(any()) }
    }

    @Test
    fun `does not enqueue when list empty`() = runTest {
        val flow = MutableSharedFlow<List<Photo>>(replay = 0)
        every { repository.getPendingUploadPhotos() } returns flow

        useCase(appScope)

        flow.emit(emptyList())

        verify(exactly = 0) { scheduler.enqueueUpload(any()) }
    }
}