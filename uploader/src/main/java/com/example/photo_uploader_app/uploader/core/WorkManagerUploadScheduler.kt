package com.example.photo_uploader_app.uploader.core

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.photo_uploader_app.domain_api.UploadEnqueueData
import com.example.photo_uploader_app.domain_api.UploadScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerUploadScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : UploadScheduler {
    override fun enqueueUpload(d: UploadEnqueueData) {
        val input = workDataOf(
            "dedupKey" to d.dedupKey,
            "localPath" to d.localPath,
            "mimeType" to d.mimeType,
            "fileExt" to d.ext
        )
        val req = OneTimeWorkRequestBuilder<UploadPhotoWorker>()
            .setInputData(input)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag("photo_upload")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("upload_photo_${d.dedupKey}", ExistingWorkPolicy.KEEP, req)
    }
}
