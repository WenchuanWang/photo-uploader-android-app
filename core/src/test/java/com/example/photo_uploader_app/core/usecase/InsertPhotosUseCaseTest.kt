package com.example.photo_uploader_app.domain.usecases

import android.net.Uri
import com.example.photo_uploader_app.core.DispatcherProvider
import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.core.usecase.InsertPhotosUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsertPhotosUseCaseTest {
    private lateinit var repository: PhotoRepository
    private lateinit var dispatcher: TestDispatcher
    private lateinit var useCase: InsertPhotosUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        dispatcher = UnconfinedTestDispatcher()
        useCase = InsertPhotosUseCase(repository, object : DispatcherProvider {
            override val io = dispatcher
        })
    }

    @Test
    fun `invoke returns early when list empty`() = runTest {
        useCase(emptyList())
        coVerify(exactly = 0) { repository.insertPhotos(any()) }
    }

    @Test
    fun `invoke calls repository with uris`() = runTest {
        val uris = listOf(mockk<Uri>())
        coEvery { repository.insertPhotos(uris) } returns Unit

        useCase(uris)

        coVerify { repository.insertPhotos(uris) }
    }

    @Test
    fun `invoke no-op on empty list`() = runTest {
        useCase(emptyList())
        coVerify(exactly = 0) { repository.insertPhotos(any()) }
    }
}