package com.ninezero.halo.core.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** 미디어 sandbox 복사 관리자 */
@Singleton
class MediaPersistenceManager @Inject constructor(  // content 권한 만료 대비 업로드 전 사본
    @param:ApplicationContext private val context: Context
) {
    enum class OwnerType(val dirName: String) {
        DRAFT("drafts"),
        PENDING_POST("pending_posts"),
        PENDING_COMMENT("pending_comments"),
        PENDING_CHAT("pending_chat")
    }

    private val rootDir: File
        get() = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }

    private fun ownerDir(type: OwnerType, ownerId: Long): File =
        File(rootDir, "${type.dirName}/$ownerId")

    suspend fun persistForOwner(
        sourceUris: List<Uri>,
        ownerType: OwnerType,
        ownerId: Long
    ): List<Uri> = withContext(Dispatchers.IO) {
        if (sourceUris.isEmpty()) return@withContext emptyList()

        val dir = ownerDir(ownerType, ownerId).apply { if (!exists()) mkdirs() }
        sourceUris.mapIndexedNotNull { index, sourceUri ->
            copyOne(sourceUri, dir, index)
        }
    }

    private fun copyOne(sourceUri: Uri, destDir: File, index: Int): Uri? {
        return try {
            val mimeType = context.contentResolver.getType(sourceUri)
                ?: guessMimeFromUri(sourceUri)
                ?: "image/jpeg"
            val extension = mimeTypeToExtension(mimeType)
            val fileName = "${index}_${UUID.randomUUID()}.$extension"
            val destFile = File(destDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            Uri.fromFile(destFile)
        } catch (e: Exception) {
            Timber.e(e, "샌드박스로 URI 복사 실패: $sourceUri")
            null
        }
    }

    private fun guessMimeFromUri(uri: Uri): String? {
        val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            ?.lowercase()?.takeIf { it.isNotBlank() } ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }

    private fun mimeTypeToExtension(mimeType: String): String {
        val subType = mimeType.substringAfter("/", "")
        return when {
            mimeType.startsWith("image/") -> when (subType) {
                "jpeg" -> "jpg"
                "" -> "jpg"
                else -> subType
            }
            mimeType.startsWith("video/") -> when (subType) {
                "quicktime" -> "mov"
                "x-msvideo" -> "avi"
                "" -> "mp4"
                else -> subType
            }
            else -> "bin"
        }
    }

    suspend fun cleanupOwner(ownerType: OwnerType, ownerId: Long) = withContext(Dispatchers.IO) {
        val dir = ownerDir(ownerType, ownerId)
        if (dir.exists()) {
            val ok = dir.deleteRecursively()
            if (!ok) Timber.w("owner 디렉터리 전체 삭제 실패: ${dir.absolutePath}")
        }
    }

    suspend fun cleanupOrphans(
        ownerType: OwnerType,
        activeOwnerIds: Set<Long>
    ) = withContext(Dispatchers.IO) {
        val typeDir = File(rootDir, ownerType.dirName)
        if (!typeDir.exists()) return@withContext
        typeDir.listFiles()?.forEach { ownerDir ->
            val id = ownerDir.name.toLongOrNull()
            if (id == null || id !in activeOwnerIds) {
                ownerDir.deleteRecursively()
                Timber.d("고아 미디어 정리 완료: ${ownerDir.absolutePath}")
            }
        }
    }
}
