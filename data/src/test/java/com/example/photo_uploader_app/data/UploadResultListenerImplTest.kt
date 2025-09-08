package com.example.photo_uploader_app.data

import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.data.data.UploadResultListenerImpl
import com.example.photo_uploader_app.data.local.PhotoDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UploadResultListenerImplTest {

    private val dao: PhotoDao = mockk(relaxed = true)
    private val listener = UploadResultListenerImpl(dao)

    @Test
    fun `onUploading marks UPLOADING`() = runTest {
        listener.onUploading("photo_7")
        coVerify { dao.updateStatusAndRemoteUrlById(7L, UploadStatus.UPLOADING) }
    }

    @Test
    fun `onFailed marks FAILED`() = runTest {
        listener.onFailed("photo_9")
        coVerify { dao.updateStatusAndRemoteUrlById(9L, UploadStatus.FAILED) }
    }

    @Test
    fun `onSuccess marks SUCCESS with url`() = runTest {
        listener.onSuccess("photo_5", "https://x")
        coVerify { dao.updateStatusAndRemoteUrlById(5L, UploadStatus.SUCCESS, "https://x") }
    }

    @Test
    fun `ignores malformed keys`() = runTest {
        listener.onSuccess("oops", "https://x")
        listener.onFailed("nope")
        listener.onUploading("bad")
        coVerify(exactly = 0) { dao.updateStatusAndRemoteUrlById(any(), any(), any()) }
    }
}


