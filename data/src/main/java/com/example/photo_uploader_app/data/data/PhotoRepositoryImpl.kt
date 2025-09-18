package com.example.photo_uploader_app.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.data.local.PhotoDao
import com.example.photo_uploader_app.data.local.PhotoEntity
import com.example.photo_uploader_app.data.mapper.toPhoto
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.domain_api.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
): PhotoRepository {

    override fun getAllPhotos(): Flow<List<Photo>> =
        photoDao.getAllPhotos().map { it.map(PhotoEntity::toPhoto) }

    override suspend fun getPendingUploadPhotos(): Flow<List<Photo>> = withContext(io) {
        photoDao.getPendingUploadPhotos().map { it.map(PhotoEntity::toPhoto) }
    }

    override suspend fun insertPhotos(uris: List<Uri>) = withContext(io) {
        if (uris.isEmpty()) return@withContext
        val cr = context.contentResolver
        val entities = uris.mapNotNull { uri ->
            try {
                val mime = cr.getType(uri) ?: "image/jpeg"
                val name = cr.query(
                    uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: "IMG_${System.currentTimeMillis()}"
                val out = copyToAppFiles(cr, uri, name, mime)

                PhotoEntity(
                    localPath = out.absolutePath,
                    mimeType = mime
                )
            } catch (_: Throwable) {
                null // skip broken items
            }
        }
        photoDao.insertPhotos(entities)
    }

    override suspend fun deletePhone(id: Long) {
        photoDao.deletePhotoById(id)
    }

    private fun copyToAppFiles(cr: ContentResolver, uri: Uri, baseName: String, mime: String): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
        val file = File(dir, "${UUID.randomUUID()}_${baseName.replace("""[^\w\-.]""".toRegex(), "_")}.$ext")
        cr.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open $uri" }
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }
}
