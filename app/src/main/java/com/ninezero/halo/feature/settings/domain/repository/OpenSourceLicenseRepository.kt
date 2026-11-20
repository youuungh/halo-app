package com.ninezero.halo.feature.settings.domain.repository

import com.mikepenz.aboutlibraries.entity.Library

interface OpenSourceLicenseRepository {
    /** 이름순 라이브러리 목록, 실패하면 빈 목록 */
    suspend fun getLibraries(): List<Library>
}
