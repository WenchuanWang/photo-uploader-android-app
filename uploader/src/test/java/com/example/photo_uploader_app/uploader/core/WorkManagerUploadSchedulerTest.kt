package com.example.photo_uploader_app.uploader.core

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.photo_uploader_app.domain_api.UploadEnqueueData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class WorkManagerUploadSchedulerTest {

    @Test
    fun `enqueueUpload enqueues unique work`() {
        val context: Context = RuntimeEnvironment.getApplication()
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        val scheduler = WorkManagerUploadScheduler(context)
        scheduler.enqueueUpload(
            UploadEnqueueData(
                dedupKey = "photo_99",
                localPath = "/tmp/a.jpg",
                mimeType = "image/jpeg",
                ext = "jpg"
            )
        )

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork("upload_photo_photo_99").get()
        assertEquals(1, infos.size)
        val info: WorkInfo = infos.first()
        assertTrue(info.tags.contains("photo_upload"))
    }
}


