package com.example.photo_uploader_app.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.photo_uploader_app.R
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.presentation.PhotoUiState
import com.example.photo_uploader_app.presentation.PhotoViewModel
import com.example.photo_uploader_app.presentation.ui.components.StatusMessage
import com.example.photo_uploader_app.presentation.ui.components.LoadingSpinner
import com.example.photo_uploader_app.presentation.ui.components.TopUploadLoadingBar
import com.example.photo_uploader_app.presentation.ui.mappers.rememberUploadStatusUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSelectorScreen(
    viewModel: PhotoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launchPicker = rememberGalleryPicker { uris ->
        viewModel.onPhotosPicked(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_gallery_screen)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (uiState is PhotoUiState.Success) {
                TopUploadLoadingBar((uiState as PhotoUiState.Success).isUploading)
            }

            Button(
                onClick = launchPicker,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.button_select_photo),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when (val state = uiState) {
                is PhotoUiState.Idle -> StatusMessage(stringResource(R.string.idle_state))
                is PhotoUiState.Uploading -> LoadingSpinner()
                is PhotoUiState.Failed ->
                    StatusMessage(
                        message = stringResource(R.string.error_upload_failed),
                        onRetry = {  }
                    )
                is PhotoUiState.Success -> {
                    PhotoContent(
                        photos = state.photoList,
                        onDeleteClick = viewModel::onPhotoDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoContent(
    photos: List<Photo>,
    onDeleteClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos) { photo ->
            PhotoCard(photo, onDeleteClick)
        }
    }
}

@Composable
private fun PhotoCard(
    photo: Photo,
    onDeleteClick: (Long) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.localPath)
                    .size(300)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected image",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { onDeleteClick(photo.id) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val uploadStatusUi = rememberUploadStatusUi(photo.status)
            Text(
                text = photo.status.name,
                style = MaterialTheme.typography.labelSmall,
                color = uploadStatusUi.tint,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            uploadStatusUi.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = "Upload Status",
                    tint = uploadStatusUi.tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberGalleryPicker(
    onImagesPicked: (List<Uri>) -> Unit
): () -> Unit {
    val context = LocalContext.current

    // Photo Picker (multiple)
    val pickMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6),
        onResult = onImagesPicked
    )

    // Fallback: ACTION_OPEN_DOCUMENT (allows multi-select + persistable URIs)
    val openMultiple = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (_: SecurityException) { /* may already be persisted or not persistable */ }
        }
        onImagesPicked(uris)
    }

    return remember(context) {
        {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                pickMultiple.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                openMultiple.launch(arrayOf("image/*"))
            }
        }
    }
}
