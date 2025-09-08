package com.example.photo_uploader_app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MimeExtTest {

    @Test
    fun `extFromMime returns correct ext for mime`() {
        val cases = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg"  to "jpg",
            "image/png"  to "png",
            "image/webp" to "webp",
            "application/octet-stream" to "jpg",
        )

        cases.forEach { (input, expected) ->
            assertEquals(expected, input.extFromMime())
        }
    }
}


