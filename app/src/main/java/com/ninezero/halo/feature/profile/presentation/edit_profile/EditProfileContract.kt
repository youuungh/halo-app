package com.ninezero.halo.feature.profile.presentation.edit_profile

import android.net.Uri
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class EditProfileState(
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val location: String = "",
    val website: String = "",
    val selectedAvatarUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val displayNameError: String? = null,
    val bioError: String? = null,
    val locationError: String? = null,
    val websiteError: String? = null,
    // 변경 감지용 원본 값
    val originalDisplayName: String = "",
    val originalBio: String = "",
    val originalLocation: String = "",
    val originalWebsite: String = "",
    val snackbarState: SnackbarState? = null
) {
    val hasChanges: Boolean
        get() = displayName != originalDisplayName ||
                bio != originalBio ||
                location != originalLocation ||
                website != originalWebsite ||
                selectedAvatarUri != null
}

sealed interface EditProfileIntent {
    data object LoadProfile : EditProfileIntent
    data class DisplayNameChanged(val displayName: String) : EditProfileIntent
    data class BioChanged(val bio: String) : EditProfileIntent
    data class LocationChanged(val location: String) : EditProfileIntent
    data class WebsiteChanged(val website: String) : EditProfileIntent
    data class AvatarSelected(val uri: Uri) : EditProfileIntent
    data object SaveProfile : EditProfileIntent
    data object NavigateBack : EditProfileIntent
    data object SnackbarDismissed : EditProfileIntent
}

sealed interface EditProfileSideEffect {
    data object NavigateBack : EditProfileSideEffect
    data class ShowToast(val message: String) : EditProfileSideEffect
}
