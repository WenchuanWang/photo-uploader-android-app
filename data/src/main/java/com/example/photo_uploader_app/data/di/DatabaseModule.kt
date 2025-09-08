package com.example.photo_uploader_app.data.di

import android.content.Context
import androidx.room.Room
import com.example.photo_uploader_app.data.local.AppDb
import com.example.photo_uploader_app.data.local.PhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDb(
        @ApplicationContext context: Context
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "photos.db").build()

    @Provides
    fun providePhotoDao(db: AppDb): PhotoDao = db.photoDao()
}
