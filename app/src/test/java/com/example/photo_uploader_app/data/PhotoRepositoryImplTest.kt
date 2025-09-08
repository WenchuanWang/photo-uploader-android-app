package com.example.photo_uploader_app.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.photo_uploader_app.data.local.PhotoDao
import com.example.photo_uploader_app.data.local.PhotoEntity
import com.example.photo_uploader_app.core.PhotoRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class PhotoRepositoryImplTest {

    private lateinit var photoDao: PhotoDao
    private lateinit var context: Context
    private lateinit var cr: ContentResolver
    private lateinit var repo: PhotoRepository

    @Before
    fun setup() {
        photoDao = mockk(relaxed = true)
        context = mockk()
        cr = mockk()
        every { context.contentResolver } returns cr

        val filesDir = File(System.getProperty("java.io.tmpdir"), "repo-test-${System.currentTimeMillis()}").apply { mkdirs() }
        every { context.filesDir } returns filesDir

        val mimeMap: MimeTypeMap = mockk()
        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeMap
        every { mimeMap.getExtensionFromMimeType("image/jpeg") } returns "jpg"

        repo = PhotoRepositoryImpl(photoDao, context)
    }

    @Test
    fun `insertPhotos copies files, builds entity, and inserts`() = runTest {
        val uri = mockk<Uri>()
        every { cr.getType(uri) } returns "image/jpeg"

        val cursor: Cursor = mockk(relaxed = true)
        every { cr.query(uri, any(), null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "photo.jpg"

        val inputData = ByteArray(4) { 1 }
        every { cr.openInputStream(uri) } returns ByteArrayInputStream(inputData)


        // When
        repo.insertPhotos(listOf(uri))

        // Then
        coVerify {
            photoDao.insertPhotos(withArg { list ->
                assertTrue(list.isNotEmpty())
                val e: PhotoEntity = list.first()
                assertTrue(e.localPath.contains("photos"))
                assertTrue(e.mimeType == "image/jpeg")
            })
        }
    }

    @Test
    fun `insertPhotos skips broken items but inserts others`() = runTest {
        // good
        val good = mockk<Uri>()
        every { cr.getType(good) } returns "image/jpeg"
        val cur1: Cursor = mockk(relaxed = true)
        every { cr.query(eq(good), any(), null, null, null) } returns cur1
        every { cur1.moveToFirst() } returns true
        every { cur1.getString(0) } returns "ok.jpg"
        every { cr.openInputStream(good) } returns ByteArrayInputStream(byteArrayOf(9,9))

        // broken (throws while opening stream)
        val broken = mockk<Uri>()
        every { cr.getType(broken) } returns "image/jpeg"
        val cur2: Cursor = mockk(relaxed = true)
        every { cr.query(eq(broken), any(), null, null, null) } returns cur2
        every { cur2.moveToFirst() } returns true
        every { cur2.getString(0) } returns "bad.jpg"
        every { cr.openInputStream(broken) } throws IllegalStateException("boom")


        // When
        repo.insertPhotos(listOf(good, broken))

        // Then: only the good one is inserted
        coVerify {
            photoDao.insertPhotos(withArg { list ->
                assertEquals(1, list.size)
                assertTrue(File(list.first().localPath).exists())
                assertEquals("image/jpeg", list.first().mimeType)
                assertEquals(null, list.first().remoteUrl)
            })
        }
        verify { cur1.close() }
        verify { cur2.close() }
    }

    @Test
    fun `insertPhotos falls back to image-jpeg when getType returns null`() = runTest {
        val uri = mockk<Uri>()
        every { cr.getType(uri) } returns null // fallback branch

        val cursor: Cursor = mockk(relaxed = true)
        every { cr.query(eq(uri), any(), null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "x.dat"

        every { cr.openInputStream(uri) } returns ByteArrayInputStream(byteArrayOf(1,2,3,4))


        // When
        repo.insertPhotos(listOf(uri))

        // Then
        coVerify {
            photoDao.insertPhotos(withArg { list ->
                assertEquals(1, list.size)
                val e = list.first()
                assertEquals("image/jpeg", e.mimeType)
                assertEquals(null, e.remoteUrl)
                assertTrue(e.localPath.endsWith(".jpg"))
            })
        }
    }

    @Test
    fun `insertPhotos sets remoteUrl null`() = runTest {
        val uri = mockk<Uri>()
        every { cr.getType(uri) } returns "image/jpeg"

        val cursor: Cursor = mockk(relaxed = true)
        every { cr.query(eq(uri), any(), null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getString(0) } returns "photo.jpg"

        every { cr.openInputStream(uri) } returns ByteArrayInputStream(byteArrayOf(1,2,3))

        // When
        repo.insertPhotos(listOf(uri))

        // Then
        coVerify {
            photoDao.insertPhotos(withArg { list ->
                assertEquals(1, list.size)
                assertNull(list.first().remoteUrl)
            })
        }
        
    }

    @Test
    fun `insertPhotos skips when list empty`() = runTest {
        repo.insertPhotos(emptyList())

        coVerify(exactly = 0) { photoDao.insertPhotos(any()) }
    }
}

 