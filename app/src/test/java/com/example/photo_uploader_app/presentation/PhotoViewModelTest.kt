package com.example.photo_uploader_app.presentation

import com.example.photo_uploader_app.MainDispatcherRule
import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.core.usecase.DeletePhotosUseCase
import com.example.photo_uploader_app.core.usecase.GetAllPhotosUseCase
import com.example.photo_uploader_app.core.usecase.InsertPhotosUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getAllPhotos: GetAllPhotosUseCase
    private lateinit var insertPhotos: InsertPhotosUseCase
    private lateinit var deletePhotos: DeletePhotosUseCase
    private lateinit var viewModel: PhotoViewModel

    @Before
    fun setup() {
        getAllPhotos = mockk(relaxed = true)
        insertPhotos = mockk(relaxed = true)
        deletePhotos = mockk(relaxed = true)
        every { getAllPhotos() } returns flow {
            emit(emptyList())
        }
        viewModel = PhotoViewModel(getAllPhotos, insertPhotos, deletePhotos)
    }

    @Test
    fun `load emits Uploading then Idle when list empty`() = runTest {
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PhotoUiState.Idle)
    }

    @Test
    fun `load emits Success and isUploading true when any pending`() = runTest {
        val photos = listOf(
            Photo(1, "/a.jpg", "jpg", UploadStatus.QUEUED),
            Photo(2, "/b.jpg", "jpg", UploadStatus.SUCCESS)
        )
        every { getAllPhotos() } returns flow { emit(photos) }

        viewModel = PhotoViewModel(getAllPhotos, insertPhotos, deletePhotos)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PhotoUiState.Success)
        val s = state as PhotoUiState.Success
        assertEquals(2, s.photoList.size)
        assertEquals(true, s.isUploading)
    }

    @Test
    fun `load emits Success and isUploading false when no pending`() = runTest {
        val photos = listOf(
            Photo(1, "/a.jpg", "jpg", UploadStatus.SUCCESS),
            Photo(2, "/b.jpg", "jpg", UploadStatus.SUCCESS)
        )
        every { getAllPhotos() } returns flow { emit(photos) }

        viewModel = PhotoViewModel(getAllPhotos, insertPhotos, deletePhotos)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PhotoUiState.Success)
        val s = state as PhotoUiState.Success
        assertEquals(2, s.photoList.size)
        assertEquals(false, s.isUploading)
    }

    @Test
    fun `onPhotosPicked delegates to use case`() = runTest {
        val uris = emptyList<android.net.Uri>()

        viewModel.onPhotosPicked(uris)
        advanceUntilIdle()

        coVerify { insertPhotos(uris) }
    }

    @Test
    fun `onPhotoDelete delegates to use case`() = runTest {
        viewModel.onPhotoDelete(5L)
        advanceUntilIdle()

        coVerify { deletePhotos(5L) }
    }
}


