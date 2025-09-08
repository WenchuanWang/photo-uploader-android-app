package com.example.photo_uploader_app.data.mapper

import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.data.local.PhotoEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoMapperTest {

    @Test
    fun `toPhoto maps fields correctly`() {
        val entity = PhotoEntity(
            id = 42L,
            localPath = "/tmp/test.jpg",
            mimeType = "image/jpeg",
            status = UploadStatus.UPLOADING,
            remoteUrl = "https://example.com/x.jpg",
            createdAt = 1234L
        )

        val model = entity.toPhoto()

        assertEquals(42L, model.id)
        assertEquals("/tmp/test.jpg", model.localPath)
        assertEquals(UploadStatus.UPLOADING, model.status)
    }
}


