package com.example.photo_uploader_app.core.util

internal fun String.extFromMime(): String = when (lowercase()) {
    "image/jpeg", "image/jpg" -> "jpg"
    "image/png"               -> "png"
    "image/webp"              -> "webp"
    else                      -> "jpg"
}