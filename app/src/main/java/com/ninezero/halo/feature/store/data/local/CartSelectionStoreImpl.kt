package com.ninezero.halo.feature.store.data.local

import com.ninezero.halo.core.local.PreferencesManager
import com.ninezero.halo.feature.store.domain.repository.CartSelectionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** 장바구니 선택 DataStore 저장소 */
@Singleton
class CartSelectionStoreImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : CartSelectionStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()  // reconcile과 remember 경합 방지

    override suspend fun reconcile(availableIds: Set<Int>): Set<Int> = mutex.withLock {
        // known은 본 적 있는 id
        val (selected, known) = preferencesManager.getCartSelection()
        // 기존 선택 + 새 항목 자동 선택
        val resolved = selected.intersect(availableIds) + (availableIds - known)
        preferencesManager.setCartSelection(resolved, availableIds)
        resolved
    }

    override fun remember(selected: Set<Int>) {
        scope.launch {
            mutex.withLock {
                preferencesManager.setCartSelectedItemIds(selected)
            }
        }
    }
}
