package com.ninezero.halo.feature.settings.presentation.open_source_licenses

import com.mikepenz.aboutlibraries.entity.Library

data class OpenSourceLicensesState(
    val isLoading: Boolean = true,
    val libraries: List<Library> = emptyList(),
    val selectedLibrary: Library? = null
)

sealed interface OpenSourceLicensesIntent {
    data class LibraryClick(val library: Library) : OpenSourceLicensesIntent
    data object DismissLicenseSheet : OpenSourceLicensesIntent
}
