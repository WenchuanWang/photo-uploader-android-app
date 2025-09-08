package com.example.photo_uploader_app.data.di

import com.example.photo_uploader_app.core.PhotoRepository
import com.example.photo_uploader_app.data.PhotoRepositoryImpl
import com.example.photo_uploader_app.data.data.UploadResultListenerImpl
import com.example.photo_uploader_app.domain_api.UploadResultListener
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindUploadResultListener(impl: UploadResultListenerImpl): UploadResultListener
}
