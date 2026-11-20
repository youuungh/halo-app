package com.ninezero.halo.core.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaFileReader @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun getFileInfo(uri: Uri): MediaUtils.FileInfo? = MediaUtils.getFileInfo(context, uri)

    fun resolveMimeType(uri: Uri): String = MediaUtils.resolveMimeType(context, uri)

    suspend fun createImageThumbnail(uri: Uri): Bitmap? = MediaUtils.createImageThumbnail(context, uri)

    suspend fun extractVideoThumbnail(uri: Uri): Bitmap? = MediaUtils.extractVideoThumbnail(context, uri)

    suspend fun getVideoDuration(uri: Uri): Long = MediaUtils.getVideoDuration(context, uri)

    suspend fun getImageDimensions(uri: Uri): Pair<Int?, Int?> = MediaUtils.getImageDimensions(context, uri)

    suspend fun getVideoDimensions(uri: Uri): Pair<Int?, Int?> = MediaUtils.getVideoDimensions(context, uri)
}
