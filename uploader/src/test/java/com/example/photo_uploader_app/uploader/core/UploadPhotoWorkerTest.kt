package com.example.photo_uploader_app.uploader.core

import android.content.Context
import android.net.Uri
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.photo_uploader_app.domain_api.UploadResultListener
import com.example.photo_uploader_app.core.DispatcherProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class UploadPhotoWorkerTest {

    private lateinit var ctx: Context
    private lateinit var params: WorkerParameters
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var resultListener: UploadResultListener

    @Before
    fun setup() {
        ctx = mockk(relaxed = true)
        params = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        resultListener = mockk(relaxed = true)
    }

    @Test
    fun `returns failure when inputs missing`() = runTest {
        every { params.inputData.getString("dedupKey") } returns null
        val worker = UploadPhotoWorker(
            ctx,
            params,
            storage,
            auth,
            resultListener,
            object : DispatcherProvider { override val io = UnconfinedTestDispatcher() }
        )
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure()::class, result::class)
        coVerify(exactly = 0) { resultListener.onFailed(any()) }
        coVerify(exactly = 0) { resultListener.onUploading(any()) }
    }

    @Test
    fun `returns failure when file missing`() = runTest {
        every { params.inputData.getString("dedupKey") } returns "photo_1"
        every { params.inputData.getString("localPath") } returns "/does/not/exist.jpg"
        every { params.inputData.getString("mimeType") } returns "image/jpeg"
        every { params.inputData.getString("fileExt") } returns "jpg"

        val worker = UploadPhotoWorker(
            ctx,
            params,
            storage,
            auth,
            resultListener,
            object : DispatcherProvider { override val io = UnconfinedTestDispatcher() }
        )
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure()::class, result::class)
        coVerify(exactly = 0) { resultListener.onUploading("photo_1") }
        coVerify(exactly = 1) { resultListener.onFailed("photo_1") }
    }

    @Test
    fun `returns success when upload completes and updates status with url`() = runTest {
        every { params.inputData.getString("dedupKey") } returns "photo_1"
        val tmp = kotlin.io.path.createTempFile().toFile().apply { writeBytes(byteArrayOf(1,2,3)) }
        every { params.inputData.getString("localPath") } returns tmp.absolutePath
        every { params.inputData.getString("mimeType") } returns "image/jpeg"
        every { params.inputData.getString("fileExt") } returns "jpg"
        every { auth.currentUser?.uid } returns "uid"

        val root: StorageReference = mockk()
        val child: StorageReference = mockk()
        every { storage.reference } returns root
        every { root.child("users/uid/photo_1.jpg") } returns child

        // Make metadata lookup succeed → uploader will skip putFile()
        every { child.metadata } returns Tasks.forResult(mockk<StorageMetadata>())
        // Provide the download URL
        every { child.downloadUrl } returns Tasks.forResult(
            Uri.parse("https://example.com/p.jpg")
        )

        val worker = UploadPhotoWorker(
            ctx,
            params,
            storage,
            auth,
            resultListener,
            object : DispatcherProvider { override val io = UnconfinedTestDispatcher() }
        )

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success()::class, result::class)
        coVerify(exactly = 1) { resultListener.onUploading("photo_1") }
        coVerify(exactly = 1) { resultListener.onSuccess("photo_1", "https://example.com/p.jpg") }
    }
}


