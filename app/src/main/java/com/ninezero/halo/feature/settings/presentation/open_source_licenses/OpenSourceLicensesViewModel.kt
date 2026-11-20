package com.ninezero.halo.feature.settings.presentation.open_source_licenses

import androidx.lifecycle.ViewModel
import com.mikepenz.aboutlibraries.entity.Library
import com.ninezero.halo.feature.settings.domain.repository.OpenSourceLicenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class OpenSourceLicensesViewModel @Inject constructor(
    private val openSourceLicenseRepository: OpenSourceLicenseRepository
) : ViewModel(), OrbitContainerHost<OpenSourceLicensesState, OpenSourceLicensesState, Nothing> {

    override val container: OrbitContainer<OpenSourceLicensesState, OpenSourceLicensesState, Nothing> =
        orbitContainer(OpenSourceLicensesState())

    init {
        loadLibraries()
    }

    fun onIntent(intent: OpenSourceLicensesIntent) {
        when (intent) {
            is OpenSourceLicensesIntent.LibraryClick -> selectLibrary(intent.library)
            is OpenSourceLicensesIntent.DismissLicenseSheet -> dismissLicenseSheet()
        }
    }

    private fun loadLibraries() = intent {
        val libraries = openSourceLicenseRepository.getLibraries()
        reduce { state.copy(isLoading = false, libraries = libraries) }
    }

    private fun selectLibrary(library: Library) = intent {
        reduce { state.copy(selectedLibrary = library) }
    }

    private fun dismissLicenseSheet() = intent {
        reduce { state.copy(selectedLibrary = null) }
    }
}
