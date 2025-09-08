package com.example.photo_uploader_app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.photo_uploader_app.core.usecase.UploadPhotosUseCase
import com.example.photo_uploader_app.di.ApplicationScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltAndroidApp
class PhotoUploaderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var uploadPhotosUseCase: UploadPhotosUseCase

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var auth: FirebaseAuth

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
        appScope.launch {
            ensureSignedIn()
        }
        uploadPhotosUseCase(appScope)
    }

    private suspend fun ensureSignedIn(): String {
        val existing = auth.currentUser
        if (existing != null) return existing.uid
        return auth.signInAnonymously().await().user!!.uid
    }
}
