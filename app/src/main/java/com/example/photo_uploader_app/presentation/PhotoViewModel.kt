package com.example.photo_uploader_app.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.core.usecase.DeletePhotosUseCase
import com.example.photo_uploader_app.core.usecase.GetAllPhotosUseCase
import com.example.photo_uploader_app.core.usecase.InsertPhotosUseCase
import com.example.photo_uploader_app.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PhotoUiState {
    data object Idle : PhotoUiState()
    data object Uploading : PhotoUiState()
    data object Failed : PhotoUiState()
    data class Success(
        val photoList: List<Photo>,
        val isUploading: Boolean
    ) : PhotoUiState()
}

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase,
    private val insertPhotosUseCase: InsertPhotosUseCase,
    private val deletePhotosUseCase: DeletePhotosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<PhotoUiState>(PhotoUiState.Idle)
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    private fun UploadStatus.isPending() =
        this == UploadStatus.UPLOADING || this == UploadStatus.QUEUED

    init {
        viewModelScope.launch {
            loadPhotos()
        }
    }

    private suspend fun loadPhotos() {
        getAllPhotosUseCase()
            .onStart { _uiState.value = PhotoUiState.Uploading }
            .map { photos ->
                if (photos.isEmpty()) {
                    PhotoUiState.Idle
                } else {
                    PhotoUiState.Success(
                        photoList = photos,
                        isUploading = photos.any { it.status.isPending() }
                    )
                }
            }
            .catch { _uiState.value = PhotoUiState.Failed }
            .collect { _uiState.value = it }
    }
    
    fun onPhotosPicked(uriList: List<Uri>) {
        viewModelScope.launch {
            insertPhotosUseCase(uriList)
        }
    }

    fun onPhotoDelete(id: Long) {
        viewModelScope.launch {
            deletePhotosUseCase(id)
        }
    }
}
