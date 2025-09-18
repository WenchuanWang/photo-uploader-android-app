package com.example.photo_uploader_app.uploader.core

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.photo_uploader_app.domain_api.IoDispatcher
import com.example.photo_uploader_app.domain_api.UploadResultListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
internal class UploadPhotoWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val resultListener: UploadResultListener,
    @IoDispatcher private val io: CoroutineDispatcher
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(io) {
        val dedup = inputData.getString("dedupKey") ?: return@withContext Result.failure()
        val localPath = inputData.getString("localPath") ?: return@withContext Result.failure()
        val mime = inputData.getString("mimeType") ?: "image/jpeg"
        val ext = inputData.getString("fileExt") ?: "jpg"

        val file = File(localPath)
        if (!file.exists()) {
            resultListener.onFailed(dedup)
            return@withContext Result.failure()
        }

        try {
            resultListener.onUploading(dedup)

            val uid = auth.currentUser?.uid
                ?: runCatching { auth.signInAnonymously().await().user?.uid }
                    .getOrNull()
                ?: return@withContext Result.retry()
            val remotePath = "users/$uid/${dedup}.$ext"

            val ref = storage.reference.child(remotePath)

            val meta = storageMetadata { contentType = mime }
            val exists = runCatching { ref.metadata.await(); true }.getOrElse { false }
            if (!exists) {
                ref.putFile(Uri.fromFile(file), meta).await()
            }

            val url = ref.downloadUrl.await().toString()

            resultListener.onSuccess(dedup, url)
            Result.success()
        } catch (t: Throwable) {
            resultListener.onFailed(dedup)
            Result.retry()
        }
    }
}
