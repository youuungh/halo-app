package com.ninezero.halo.core.util

import java.util.concurrent.atomic.AtomicInteger

/** 목록 세대 번호, 첫 페이지로 목록을 갈아끼울 때마다 올림 */
class ListGeneration {  // 페이지 번호 비교는 새로고침 뒤 같은 번호로 돌아오면 못 잡음
    private val value = AtomicInteger(0)

    fun current(): Int = value.get()

    fun next(): Int = value.incrementAndGet()
}
