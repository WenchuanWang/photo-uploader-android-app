package com.example.photo_uploader_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.photo_uploader_app.core.model.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert
    suspend fun insertPhotos(entities: List<PhotoEntity>)

    @Query("SELECT * FROM photos ORDER BY createdAt ASC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE status IS NOT 'SUCCESS' ORDER BY createdAt ASC")
    fun getPendingUploadPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    @Query("UPDATE photos SET status = :status, remoteUrl = :remoteUrl WHERE id = :photoId")
    suspend fun updateStatusAndRemoteUrlById(
        photoId: Long,
        status: UploadStatus,
        remoteUrl: String? = null
    )

    @Query("SELECT id FROM photos WHERE localPath IN (:paths) ORDER BY createdAt ASC")
    suspend fun getIdsByLocalPaths(paths: List<String>): List<Long>
}
