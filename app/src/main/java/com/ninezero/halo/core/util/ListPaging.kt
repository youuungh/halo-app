package com.ninezero.halo.core.util

import java.util.concurrent.atomic.AtomicInteger

/** 목록 세대 번호, 첫 페이지로 목록을 갈아끼울 때마다 올림 */
class ListGeneration {  // 페이지 번호 비교는 새로고침 뒤 같은 번호로 돌아오면 못 잡음
    private val value = AtomicInteger(0)

    fun current(): Int = value.get()

    fun next(): Int = value.incrementAndGet()
}

/** 새 첫 페이지 뒤에 기존 목록 나머지를 붙임, 시간순 목록 전용 */
fun <T> List<T>.mergeFreshHead(freshHead: List<T>, id: (T) -> Any): List<T> {
    val positions = withIndex().associate { (index, item) -> id(item) to index }
    // 새 페이지 끝에서 찾은 첫 공통 항목이 경계
    val boundary = freshHead.asReversed().firstNotNullOfOrNull { positions[id(it)] } ?: -1
    val tail = if (boundary >= 0) drop(boundary + 1) else emptyList()
    return (freshHead + tail).distinctBy(id)
}
