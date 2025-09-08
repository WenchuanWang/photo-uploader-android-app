package com.example.photo_uploader_app.presentation.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.photo_uploader_app.R
import com.example.photo_uploader_app.core.model.UploadStatus
import com.example.photo_uploader_app.domain.model.Photo
import com.example.photo_uploader_app.presentation.PhotoUiState
import com.example.photo_uploader_app.presentation.PhotoViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoSelectorScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsIdle_thenSelectButton_clickable() {
        val vm: PhotoViewModel = mockk(relaxed = true)
        every { vm.uiState } returns MutableStateFlow<PhotoUiState>(PhotoUiState.Idle)

        composeRule.setContent {
            PhotoSelectorScreen(viewModel = vm)
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.idle_state)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_select_photo)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_select_photo)).performClick()
    }

    @Test
    fun showsPhotos_and_deleteCallback_invoked() {
        val photos = listOf(
            Photo(id = 1, localPath = "/tmp/a.jpg", "jpg", status = UploadStatus.SUCCESS),
            Photo(id = 2, localPath = "/tmp/b.jpg", "jpg", status = UploadStatus.QUEUED)
        )
        val vm: PhotoViewModel = mockk(relaxed = true)
        every { vm.uiState } returns MutableStateFlow<PhotoUiState>(PhotoUiState.Success(photos, isUploading = true))

        composeRule.setContent {
            PhotoSelectorScreen(viewModel = vm)
        }

        // Top bar title and loading bar visible
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.title_gallery_screen)).assertIsDisplayed()
        // Grid items' status text should be shown
        composeRule.onNodeWithText("SUCCESS").assertIsDisplayed()
        composeRule.onNodeWithText("QUEUED").assertIsDisplayed()

        // Click the first delete (Close) icon; semantics are provided by the Icon's contentDescription
        composeRule.onAllNodesWithContentDescription("Close").onFirst().performClick()
        composeRule.runOnIdle {
            verify(exactly = 1) { vm.onPhotoDelete(1L) }
        }
    }
}


