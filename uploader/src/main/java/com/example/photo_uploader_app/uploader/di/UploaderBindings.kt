package com.example.photo_uploader_app.uploader.di

import com.example.photo_uploader_app.domain_api.UploadScheduler
import com.example.photo_uploader_app.uploader.core.WorkManagerUploadScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UploaderBindings {
    @Binds
    @Singleton
    abstract fun bindScheduler(impl: WorkManagerUploadScheduler): UploadScheduler
}
