package com.ninezero.halo.core.database

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAccountData @Inject constructor(
    private val database: HaloDatabase
) {
    suspend fun clear() {
        try {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "계정 로컬 데이터 정리 실패")
        }
    }
}
