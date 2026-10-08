package com.ninezero.halo.feature.profile.presentation.edit_profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.profile.presentation.edit_profile.components.AvatarSection
import com.ninezero.halo.feature.profile.presentation.edit_profile.components.EditProfileTextField
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is EditProfileSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is EditProfileSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(EditProfileIntent.SnackbarDismissed) }
    )

    // 로딩은 Content 안에서 본문만 덮음
    EditProfileScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )

    if (state.isSaving) {
        LoadingDialog()
    }
}

@Composable
internal fun EditProfileScreenContent(
    state: EditProfileState,
    onIntent: (EditProfileIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    val focusManager = LocalFocusManager.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onIntent(EditProfileIntent.AvatarSelected(it)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
    ) {
        // 스크롤 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 24.dp))

            AvatarSection(
                avatarUrl = state.avatarUrl,
                selectedAvatarUri = state.selectedAvatarUri,
                onAvatarClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditProfileTextField(
                    value = state.displayName,
                    onValueChange = { onIntent(EditProfileIntent.DisplayNameChanged(it)) },
                    label = "닉네임",
                    maxLength = 30,
                    showCounter = true,
                    isError = state.displayNameError != null,
                    errorMessage = state.displayNameError,
                    singleLine = true
                )

                EditProfileTextField(
                    value = state.bio,
                    onValueChange = { onIntent(EditProfileIntent.BioChanged(it)) },
                    label = "소개",
                    maxLength = 300,
                    showCounter = true,
                    isError = state.bioError != null,
                    errorMessage = state.bioError,
                    singleLine = false,
                    maxLines = 5,
                    minHeight = 120.dp
                )

                EditProfileTextField(
                    value = state.location,
                    onValueChange = { onIntent(EditProfileIntent.LocationChanged(it)) },
                    label = "위치",
                    maxLength = 100,
                    isError = state.locationError != null,
                    errorMessage = state.locationError,
                    singleLine = true,
                    leadingIcon = painterResource(id = R.drawable.ic_location)
                )

                EditProfileTextField(
                    value = state.website,
                    onValueChange = { onIntent(EditProfileIntent.WebsiteChanged(it)) },
                    label = "웹사이트",
                    maxLength = 200,
                    isError = state.websiteError != null,
                    errorMessage = state.websiteError,
                    singleLine = true,
                    leadingIcon = painterResource(id = R.drawable.ic_link),
                    keyboardType = KeyboardType.Uri
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 로딩/에러 오버레이
        if (state.isLoading || state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(top = headerHeight),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CenteredLoadingIndicator(size = LoadingIndicatorSize.Large)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { onIntent(EditProfileIntent.LoadProfile) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.bounceClick()
                        ) {
                            Text("재시도")
                        }
                    }
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0.5f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = { onIntent(EditProfileIntent.NavigateBack) },
                    icon = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "프로필 수정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                val isSaveEnabled = state.hasChanges && !state.isSaving
                GlassActionButton(
                    onClick = { onIntent(EditProfileIntent.SaveProfile) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    hazeState = hazeState,
                    enabled = isSaveEnabled
                ) {
                    Text(
                        text = "저장",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

