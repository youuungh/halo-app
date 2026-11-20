package com.ninezero.halo.feature.subscription.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FullScreenBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.modifiers.bringIntoViewOnFocus
import com.ninezero.halo.core.ui.components.selector.SegmentedTabRow
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.GroupShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun SubscriptionPlanBottomSheet(
    plan: SubscriptionPlan?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, tier: SubscriptionPlanTier, description: String, price: String, benefits: List<String>) -> Unit,
    saveError: String? = null,
    onSaveErrorDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf(plan?.name ?: "") }
    var selectedTier by remember { mutableStateOf(plan?.tier ?: SubscriptionPlanTier.TIER1) }
    var description by remember { mutableStateOf(plan?.description ?: "") }
    var price by remember { mutableStateOf(plan?.price?.toString() ?: "") }
    var benefits by remember { mutableStateOf(plan?.benefits ?: emptyList()) }
    var newBenefit by remember { mutableStateOf("") }

    val isEditMode = plan != null
    val isFormValid = name.isNotBlank() && price.isNotBlank()
            && description.isNotBlank() && benefits.isNotEmpty()

    if (isSaving) {
        LoadingDialog()
    }

    FullScreenBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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

            // 시트 window에 가려지지 않게 시트 안에서 표시
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    SnackbarHandler(
        message = saveError,
        hostState = snackbarHostState,
        onDismiss = onSaveErrorDismiss
    )
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
            // 티어는 분할 탭, 수정할 땐 잠김
            item {
                FormSection(title = "티어") {
                    val tiers = listOf(SubscriptionPlanTier.TIER1, SubscriptionPlanTier.TIER2)
                    SegmentedTabRow(
                        tabs = tiers.map { it.label },
                        selectedIndex = tiers.indexOf(selectedTier).coerceAtLeast(0),
                        onTabSelected = { if (!isEditMode) onTierChange(tiers[it]) },
                        modifier = Modifier.alpha(if (isEditMode) 0.5f else 1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormSection(title = "이름") {
                            FormTextField(
                                value = name,
                                onValueChange = onNameChange,
                                placeholder = "Basic",
                                maxLength = 50,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Right) },
                                isError = showErrors && name.isBlank(),
                                errorMessage = "이름을 입력해주세요"
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormSection(title = "월 가격") {
                            FormTextField(
                                value = price,
                                onValueChange = { onPriceChange(it.filter { c -> c.isDigit() }) },
                                placeholder = "4,900",
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
                }
            }

            item {
                FormSection(title = "설명") {
                    FormTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        placeholder = "기본 혜택을 누려보세요",
                        singleLine = false,
                        maxLength = 500,
                        isError = showErrors && description.isBlank(),
                        errorMessage = "설명을 입력해주세요"
                    )
                }
            }

            // 혜택: 고정 · 내 혜택 · '혜택 추가' 줄
            item {
                FormSection(title = "혜택") {
                    BenefitGroup(
                        fixedBenefits = listOf(
                            if (selectedTier == SubscriptionPlanTier.TIER2) "구매 금액의 10% 포인트 적립" else "구매 금액의 5% 포인트 적립",
                            "스토어 상품 무료배송"
                        ),
                        benefits = benefits,
                        onBenefitRemove = onBenefitRemove,
                        newBenefit = newBenefit,
                        onNewBenefitChange = onNewBenefitChange,
                        onAddBenefit = onAddBenefit
                    )
                    if (showErrors && benefits.isEmpty()) {
                        Text(
                            text = "혜택을 하나 이상 추가해주세요",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }
                }
            }
        }

        SheetHeader(
            title = if (isEditMode) "멤버십 수정" else "새 멤버십",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it },
            closeDescription = "취소",
            onConfirm = {
                if (isFormValid) onSave()
                else showErrors = true
            },
            confirmDescription = "저장"
        )
    }
}

@Composable
private fun BenefitGroup(
    fixedBenefits: List<String>,
    benefits: List<String>,
    onBenefitRemove: (Int) -> Unit,
    newBenefit: String,
    onNewBenefitChange: (String) -> Unit,
    onAddBenefit: () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    var isAdding by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .border(0.5.dp, lineColor, GroupShape)
    ) {
        fixedBenefits.forEachIndexed { index, text ->
            if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
            BenefitRow(text = text) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = "기본 혜택",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        benefits.forEachIndexed { index, text ->
            HorizontalDivider(thickness = 0.5.dp, color = lineColor)
            BenefitRow(text = text) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "삭제",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onBenefitRemove(index) }
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = lineColor)
        if (isAdding) {
            // 그 줄이 입력칸이 되고 오른쪽 '추가'
            val canAdd = newBenefit.isNotBlank()
            var isInputFocused by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(isInputFocused)  // 맨 아래 줄이라 키보드에 가리지 않게
                    .padding(start = 14.dp, end = 6.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = newBenefit,
                    onValueChange = { if (it.length <= 50) onNewBenefitChange(it) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isInputFocused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canAdd) onAddBenefit() }),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box {
                            if (newBenefit.isEmpty()) {
                                Text(
                                    text = "혜택",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            inner()
                        }
                    }
                )
                Text(
                    text = "추가",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canAdd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(enabled = canAdd, onClick = onAddBenefit)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAdding = true }
                    .padding(horizontal = 14.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "혜택 추가",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(
    text: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 6.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            trailing()
        }
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
