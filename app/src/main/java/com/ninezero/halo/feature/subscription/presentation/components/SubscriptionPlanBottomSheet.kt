package com.ninezero.halo.feature.subscription.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun SubscriptionPlanBottomSheet(
    plan: SubscriptionPlan?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, tier: SubscriptionPlanTier, description: String, price: String, benefits: List<String>) -> Unit,
    toastMessage: String? = null,
    onToastDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(plan?.name ?: "") }
    var selectedTier by remember { mutableStateOf(plan?.tier ?: SubscriptionPlanTier.TIER1) }
    var description by remember { mutableStateOf(plan?.description ?: "") }
    var price by remember { mutableStateOf(plan?.price ?: "") }
    var benefits by remember { mutableStateOf(plan?.benefits ?: emptyList()) }
    var newBenefit by remember { mutableStateOf("") }

    val isEditMode = plan != null
    val isFormValid = name.isNotBlank() && price.isNotBlank()
            && description.isNotBlank() && benefits.isNotEmpty()

    if (isSaving) {
        LoadingDialog()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box {
            SubscriptionPlanBottomSheetContent(
                isEditMode = isEditMode,
                name = name,
                onNameChange = { name = it },
                selectedTier = selectedTier,
                onTierChange = { selectedTier = it },
                description = description,
                onDescriptionChange = { description = it },
                price = price,
                onPriceChange = { price = it },
                benefits = benefits,
                onBenefitRemove = { index ->
                    benefits = benefits.toMutableList().apply { removeAt(index) }
                },
                newBenefit = newBenefit,
                onNewBenefitChange = { newBenefit = it },
                onAddBenefit = {
                    if (newBenefit.isNotBlank()) {
                        benefits = benefits + newBenefit
                        newBenefit = ""
                    }
                },
                isFormValid = isFormValid,
                onDismiss = onDismiss,
                onSave = { onSave(name, selectedTier, description, price, benefits) }
            )

            // 토스트는 시트 window에 가려지므로 시트 안에서 표시
            CenterToast(
                visible = toastMessage != null,
                message = toastMessage ?: "",
                onDismiss = onToastDismiss
            )
        }
    }
}

@Composable
private fun SubscriptionPlanBottomSheetContent(
    isEditMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    selectedTier: SubscriptionPlanTier,
    onTierChange: (SubscriptionPlanTier) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    benefits: List<String>,
    onBenefitRemove: (Int) -> Unit,
    newBenefit: String,
    onNewBenefitChange: (String) -> Unit,
    onAddBenefit: () -> Unit,
    isFormValid: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    initialShowErrors: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    var headerHeight by remember { mutableStateOf(0.dp) }
    var showErrors by remember { mutableStateOf(initialShowErrors) }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .blockSheetFling()
                .hazeSource(hazeState),
            contentPadding = PaddingValues(
                top = headerHeight + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 32.dp + navBottom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            overscrollEffect = null
        ) {
            item {
                FormSection(title = "멤버십 이름") {
                    FormTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = "예: Basic, Premium",
                        maxLength = 50,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        isError = showErrors && name.isBlank(),
                        errorMessage = "멤버십명을 입력해주세요",
                        showCounter = true
                    )
                }
            }

            // 티어 선택, 수정 모드 비활성
            item {
                FormSection(title = "티어") {
                    TierSelector(
                        selectedTier = selectedTier,
                        onTierSelect = onTierChange,
                        enabled = !isEditMode
                    )
                }
            }

            item {
                FormSection(title = "가격 (원/월)") {
                    FormTextField(
                        value = price,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            onPriceChange(filtered)
                        },
                        placeholder = "예: 4,900",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        prefix = "₩",
                        isError = showErrors && price.isBlank(),
                        errorMessage = "가격을 입력해주세요",
                        numberFormat = true
                    )
                }
            }

            item {
                FormSection(title = "설명") {
                    FormTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        placeholder = "멤버십에 대한 간단한 설명",
                        singleLine = false,
                        minLines = 2,
                        maxLength = 500,
                        isError = showErrors && description.isBlank(),
                        errorMessage = "멤버십 설명을 입력해주세요",
                        showCounter = true
                    )
                }
            }

            item {
                FormSection(title = "혜택") {
                    FixedBenefitItem(
                        text = if (selectedTier == SubscriptionPlanTier.TIER2) "구매 금액의 10% 포인트 적립" else "구매 금액의 5% 포인트 적립"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FixedBenefitItem(text = "스토어 상품 무료배송")
                    Spacer(modifier = Modifier.height(10.dp))

                    BenefitInputField(
                        value = newBenefit,
                        onValueChange = onNewBenefitChange,
                        onAddClick = onAddBenefit
                    )
                    if (showErrors && benefits.isEmpty()) {
                        Text(
                            text = "최소 하나 이상의 혜택을 입력해주세요",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (benefits.isNotEmpty()) {
                items(benefits.size) { index ->
                    BenefitItem(
                        benefit = benefits[index],
                        onRemove = { onBenefitRemove(index) }
                    )
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
                        startIntensity = 1f,
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
            BottomSheetDragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "취소",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Text(
                    text = if (isEditMode) "멤버십 수정" else "새 멤버십 생성",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                GlassIconButton(
                    onClick = {
                        if (isFormValid) onSave()
                        else showErrors = true
                    },
                    icon = painterResource(R.drawable.ic_check),
                    contentDescription = "저장",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )
            }
        }
    }
}

@Composable
private fun TierSelector(
    selectedTier: SubscriptionPlanTier,
    onTierSelect: (SubscriptionPlanTier) -> Unit,
    enabled: Boolean = true
) {
    val tiers = listOf(
        SubscriptionPlanTier.TIER1 to "Tier 1",
        SubscriptionPlanTier.TIER2 to "Tier 2"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tiers.forEach { (tier, label) ->
            val isSelected = selectedTier == tier
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled) { onTierSelect(tier) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val isAddEnabled = value.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 50) onValueChange(it) },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (isAddEnabled) onAddClick() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "혜택 입력 (예: 전용 콘텐츠 열람)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isAddEnabled) {
                        MaterialTheme.colorScheme.inverseSurface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .clickable(enabled = isAddEnabled, onClick = onAddClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "추가",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isAddEnabled) {
                    MaterialTheme.colorScheme.inverseOnSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun FixedBenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BenefitItem(
    benefit: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = benefit,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "삭제",
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onRemove)
                .padding(2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SubscriptionPlanBottomSheetCreatePreview() {
    HaloTheme {
        Surface {
            SubscriptionPlanBottomSheetContent(
                isEditMode = false,
                name = "",
                onNameChange = {},
                selectedTier = SubscriptionPlanTier.TIER1,
                onTierChange = {},
                description = "",
                onDescriptionChange = {},
                price = "",
                onPriceChange = {},
                benefits = emptyList(),
                onBenefitRemove = {},
                newBenefit = "",
                onNewBenefitChange = {},
                onAddBenefit = {},
                isFormValid = false,
                onDismiss = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SubscriptionPlanBottomSheetEditPreview() {
    HaloTheme {
        Surface {
            SubscriptionPlanBottomSheetContent(
                isEditMode = true,
                name = "Premium",
                onNameChange = {},
                selectedTier = SubscriptionPlanTier.TIER2,
                onTierChange = {},
                description = "프리미엄 혜택을 누려보세요",
                onDescriptionChange = {},
                price = "9,900",
                onPriceChange = {},
                benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여", "1:1 메시지"),
                onBenefitRemove = {},
                newBenefit = "",
                onNewBenefitChange = {},
                onAddBenefit = {},
                isFormValid = true,
                onDismiss = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SubscriptionPlanBottomSheetValidationPreview() {
    HaloTheme {
        Surface {
            SubscriptionPlanBottomSheetContent(
                isEditMode = false,
                name = "",
                onNameChange = {},
                selectedTier = SubscriptionPlanTier.TIER1,
                onTierChange = {},
                description = "",
                onDescriptionChange = {},
                price = "",
                onPriceChange = {},
                benefits = emptyList(),
                onBenefitRemove = {},
                newBenefit = "",
                onNewBenefitChange = {},
                onAddBenefit = {},
                isFormValid = false,
                initialShowErrors = true,
                onDismiss = {},
                onSave = {}
            )
        }
    }
}
