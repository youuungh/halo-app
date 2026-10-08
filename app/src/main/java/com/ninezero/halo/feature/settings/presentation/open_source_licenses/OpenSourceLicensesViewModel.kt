package com.ninezero.halo.feature.settings.presentation.open_source_licenses

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.ninezero.halo.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OpenSourceLicensesViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
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

    private fun loadLibraries() = intent {  // aboutlibraries.json에서 로드
        val libraries = withContext(Dispatchers.IO) {
            runCatching {
                val json = context.resources.openRawResource(R.raw.aboutlibraries)
                    .bufferedReader().use { it.readText() }
                Libs.Builder().withJson(json).build()
                    .libraries
                    .sortedBy { it.name.lowercase() }
            }.onFailure { Timber.e(it, "오픈소스 라이선스 정보 로드 실패") }
                .getOrDefault(emptyList())
        }
        reduce { state.copy(isLoading = false, libraries = libraries) }
    }

    private fun selectLibrary(library: Library) = intent {
        reduce { state.copy(selectedLibrary = library) }
    }

    private fun dismissLicenseSheet() = intent {
        reduce { state.copy(selectedLibrary = null) }
    }
}
