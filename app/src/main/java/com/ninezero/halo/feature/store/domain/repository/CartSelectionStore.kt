package com.ninezero.halo.feature.store.domain.repository

interface CartSelectionStore {
    /** 현재 항목 기준 선택 정리 */
    suspend fun reconcile(availableIds: Set<Int>): Set<Int>  // 새 항목은 자동 선택

    /** 선택 저장 */
    fun remember(selected: Set<Int>)
}
