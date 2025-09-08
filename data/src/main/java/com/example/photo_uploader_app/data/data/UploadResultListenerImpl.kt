package com.example.photo_uploader_app.data.data

import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.data.local.PhotoDao
import com.example.photo_uploader_app.domain_api.UploadResultListener
import javax.inject.Inject

class UploadResultListenerImpl @Inject constructor(
    private val photoDao: PhotoDao
): UploadResultListener {

    override suspend fun onFailed(dedupKey: String) {
        val id = dedupKey.getPhotoId() ?: return
        photoDao.updateStatusAndRemoteUrlById(id, UploadStatus.FAILED)
    }

    override suspend fun onUploading(dedupKey: String) {
        val id = dedupKey.getPhotoId() ?: return
        photoDao.updateStatusAndRemoteUrlById(id, UploadStatus.UPLOADING)
    }

    override suspend fun onSuccess(dedupKey: String, downloadUrl: String) {
        val id = dedupKey.getPhotoId() ?: return
        photoDao.updateStatusAndRemoteUrlById(id, UploadStatus.SUCCESS, downloadUrl)
    }

    private fun String.getPhotoId() = removePrefix("photo_").toLongOrNull()
}
