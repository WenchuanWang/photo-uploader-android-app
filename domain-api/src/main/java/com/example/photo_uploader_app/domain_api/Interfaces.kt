package com.example.photo_uploader_app.domain_api

data class UploadEnqueueData(
    val dedupKey: String,
    val localPath: String,
    val mimeType: String,
    val ext: String
)

interface UploadScheduler {
    fun enqueueUpload(data: UploadEnqueueData)
}

interface UploadResultListener {
    suspend fun onUploading(dedupKey: String)
    suspend fun onSuccess(dedupKey: String, downloadUrl: String)
    suspend fun onFailed(dedupKey: String)
}
