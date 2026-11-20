package com.ninezero.halo.core.datastore

enum class ThemeMode(val storageValue: Int, val label: String, val sheetLabel: String) {
    LIGHT(0, "라이트", "라이트"),
    DARK(1, "다크", "다크"),
    SYSTEM(2, "자동", "자동 (시스템 설정)");

    companion object {
        fun fromStorage(value: Int?): ThemeMode = entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}
