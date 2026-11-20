package com.ninezero.halo.feature.settings.data.repository

import android.content.Context
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.ninezero.halo.R
import com.ninezero.halo.feature.settings.domain.repository.OpenSourceLicenseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OpenSourceLicenseRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : OpenSourceLicenseRepository {

    override suspend fun getLibraries(): List<Library> = withContext(Dispatchers.IO) {
        try {
            val json = context.resources.openRawResource(R.raw.aboutlibraries)  // aboutlibraries.json
                .bufferedReader().use { it.readText() }
            Libs.Builder().withJson(json).build()
                .libraries
                .sortedBy { it.name.lowercase() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "오픈소스 라이선스 정보 로드 실패")
            emptyList()
        }
    }
}
