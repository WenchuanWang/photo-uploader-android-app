package com.example.photo_uploader_app.presentation.ui.mappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.photo_uploader_app.core.model.UploadStatus

data class UploadStatusUi(
    val icon: ImageVector?,
    val tint: Color
)

@Composable
fun rememberUploadStatusUi(status: UploadStatus): UploadStatusUi {
    val cs = MaterialTheme.colorScheme
    return when (status) {
        UploadStatus.QUEUED, UploadStatus.UPLOADING -> UploadStatusUi(null, cs.primary)
        UploadStatus.SUCCESS -> UploadStatusUi(Icons.Outlined.CheckCircle, Color.Green)
        UploadStatus.FAILED -> UploadStatusUi(Icons.Outlined.Warning, Color.Red)
    }
}
