package com.ninezero.halo.core.local

object ThemeMode {
    const val LIGHT = 0
    const val DARK = 1
    const val SYSTEM = 2

    fun toDisplayName(mode: Int): String = when (mode) {
        LIGHT -> "라이트"
        DARK -> "다크"
        else -> "자동"
    }
}
