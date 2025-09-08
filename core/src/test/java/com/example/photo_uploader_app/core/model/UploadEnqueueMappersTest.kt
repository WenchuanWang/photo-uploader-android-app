package com.example.photo_uploader_app.core.model

import com.example.photo_uploader_app.domain.model.Photo
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadEnqueueMappersTest {

    @Test
    fun `maps photo to enqueue data correctly`() {
        val photo = Photo(id = 10L, localPath = "/tmp/a.jpg", mimeType = "image/jpeg")

        val data = photo.toUploadEnqueueData()

        assertEquals("photo_10", data.dedupKey)
        assertEquals("/tmp/a.jpg", data.localPath)
        assertEquals("image/jpeg", data.mimeType)
        assertEquals("jpg", data.ext)
    }
}
