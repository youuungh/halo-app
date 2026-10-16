package com.ninezero.halo.feature.tag.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.chip.TagChipGroup
import com.ninezero.halo.core.ui.components.input.TagInputField
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.tag.domain.model.Tag
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 공용 태그 시트 */
@Composable
fun TagBottomSheet(
    sheetState: SheetState,
    isCreator: Boolean,
    tags: List<String>,
    sectionTags: List<Tag>,
    selectedSectionTagId: Int?,
    isLoadingSectionTags: Boolean,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSelectSectionTag: (Int?) -> Unit,
    onCreateSectionTag: (String) -> Unit,
    onUpdateSectionTag: (Int, String) -> Unit,
    onDeleteSectionTag: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    snackbarMessage: String? = null,
    onSnackbarDismiss: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        val hazeState = remember { HazeState() }

        BottomSheetDragHandle()

        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                text = "태그",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            GlassIconButton(
                onClick = onConfirm,
                icon = painterResource(R.drawable.ic_check),
                contentDescription = "확인",
                hazeState = hazeState,
                backgroundColor = MaterialTheme.colorScheme.surface,
                size = 36.dp,
                iconSize = 20.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.hazeSource(hazeState)) {
            if (isCreator) {
                CreatorTagContent(
                    tags = tags,
                    sectionTags = sectionTags,
                    selectedSectionTagId = selectedSectionTagId,
                    isLoadingSectionTags = isLoadingSectionTags,
                    onAddTag = onAddTag,
                    onRemoveTag = onRemoveTag,
                    onSelectSectionTag = onSelectSectionTag,
                    onCreateSectionTag = onCreateSectionTag,
                    onUpdateSectionTag = onUpdateSectionTag,
                    onDeleteSectionTag = onDeleteSectionTag
                )
            } else {
                NormalTagContent(
                    tags = tags,
                    onAddTag = onAddTag,
                    onRemoveTag = onRemoveTag
                )
            }

            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    SnackbarHandler(
        message = snackbarMessage,
        hostState = snackbarHostState,
        onDismiss = onSnackbarDismiss
    )
}

@Composable
private fun NormalTagContent(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        TagInputField(
            value = inputValue,
            onValueChange = { inputValue = it },
            onAddClick = {
                if (inputValue.isNotBlank()) {
                    onAddTag(inputValue)
                    inputValue = ""
                }
            },
            placeholder = "태그 입력"
        )

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            TagChipGroup(
                tags = tags,
                onDeleteClick = onRemoveTag
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CreatorTagContent(
    tags: List<String>,
    sectionTags: List<Tag>,
    selectedSectionTagId: Int?,
    isLoadingSectionTags: Boolean,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSelectSectionTag: (Int?) -> Unit,
    onCreateSectionTag: (String) -> Unit,
    onUpdateSectionTag: (Int, String) -> Unit,
    onDeleteSectionTag: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("일반태그", "섹션태그")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        TagTabRow(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                NormalTagTabContent(
                    tags = tags,
                    onAddTag = onAddTag,
                    onRemoveTag = onRemoveTag
                )
            }
            1 -> {
                SectionTagTabContent(
                    sectionTags = sectionTags,
                    selectedSectionTagId = selectedSectionTagId,
                    isLoading = isLoadingSectionTags,
                    onSelectSectionTag = onSelectSectionTag,
                    onCreateSectionTag = onCreateSectionTag,
                    onUpdateSectionTag = onUpdateSectionTag,
                    onDeleteSectionTag = onDeleteSectionTag
                )
            }
        }
    }
}

@Composable
private fun NormalTagTabContent(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TagInputField(
            value = inputValue,
            onValueChange = { inputValue = it },
            onAddClick = {
                if (inputValue.isNotBlank()) {
                    onAddTag(inputValue)
                    inputValue = ""
                }
            },
            placeholder = "태그 입력"
        )

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            TagChipGroup(
                tags = tags,
                onDeleteClick = onRemoveTag
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTagTabContent(
    sectionTags: List<Tag>,
    selectedSectionTagId: Int?,
    isLoading: Boolean,
    onSelectSectionTag: (Int?) -> Unit,
    onCreateSectionTag: (String) -> Unit,
    onUpdateSectionTag: (Int, String) -> Unit,
    onDeleteSectionTag: (Int) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    var editingTagId by remember { mutableStateOf<Int?>(null) }
    var editingValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TagInputField(
            value = inputValue,
            onValueChange = { inputValue = it },
            onAddClick = {
                if (inputValue.isNotBlank()) {
                    onCreateSectionTag(inputValue)
                    inputValue = ""
                }
            },
            placeholder = "새 섹션 이름",
            // 로드·편집 중엔 막아 입력 유실 방지
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 섹션 태그 목록
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (isLoading) {
                CenteredLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    size = LoadingIndicatorSize.Medium
                )
            } else if (sectionTags.isEmpty()) {
                // 빈 상태: 선택안함만 표시
                SectionTagItem(
                    tag = null,
                    isSelected = true,
                    isEditing = false,
                    editingValue = "",
                    onSelect = { onSelectSectionTag(null) },
                    onEditClick = {},
                    onDeleteClick = {},
                    onEditValueChange = {},
                    onEditConfirm = {}
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .blockSheetFling(),
                    overscrollEffect = null
                ) {
                    // 선택 안함 옵션
                    item {
                        SectionTagItem(
                            tag = null,
                            isSelected = selectedSectionTagId == null,
                            isEditing = false,
                            editingValue = "",
                            onSelect = { onSelectSectionTag(null) },
                            onEditClick = {},
                            onDeleteClick = {},
                            onEditValueChange = {},
                            onEditConfirm = {}
                        )
                    }

                    items(
                        items = sectionTags,
                        key = { it.id }
                    ) { tag ->
                        val isEditing = editingTagId == tag.id
                        SectionTagItem(
                            tag = tag,
                            isSelected = selectedSectionTagId == tag.id,
                            isEditing = isEditing,
                            editingValue = if (isEditing) editingValue else tag.name,
                            onSelect = { onSelectSectionTag(tag.id) },
                            onEditClick = {
                                editingTagId = tag.id
                                editingValue = tag.name
                            },
                            onDeleteClick = { onDeleteSectionTag(tag.id) },
                            onEditValueChange = { editingValue = it },
                            onEditConfirm = {
                                if (editingValue.isNotBlank() && editingValue != tag.name) {
                                    onUpdateSectionTag(tag.id, editingValue)
                                }
                                editingTagId = null
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTagItem(
    tag: Tag?,
    isSelected: Boolean,
    isEditing: Boolean,
    editingValue: String,
    onSelect: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditValueChange: (String) -> Unit,
    onEditConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크 아이콘, 선택 시만
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "선택됨",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (tag == null) {
            Text(
                text = "선택 안함",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        } else if (isEditing) {
            TagInputField(
                value = editingValue,
                onValueChange = onEditValueChange,
                onAddClick = onEditConfirm,
                placeholder = "섹션 이름",
                buttonText = "완료",
                isSubtleButton = true,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (tag.itemCount > 0) {
                // count 배지
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tag.itemCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            // 수정 버튼
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "수정",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 삭제 버튼
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable(onClick = onDeleteClick)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TagTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var tabSize by remember { mutableStateOf(IntSize.Zero) }

    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (tabSize.width * selectedIndex).toDp() },
        label = "indicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorOffset + 2.dp).roundToPx(), 0) }
                .padding(vertical = 2.dp)
                .width(with(density) { tabSize.width.toDp() } - 4.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .onSizeChanged { size ->
                            if (tabSize.width == 0) {
                                tabSize = size
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun NormalTagContentPreview() {
    HaloTheme {
        Surface {
            NormalTagContent(
                tags = listOf("일상", "브이로그", "작업물"),
                onAddTag = {},
                onRemoveTag = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorTagContentPreview() {
    HaloTheme {
        Surface {
            CreatorTagContent(
                tags = listOf("일상", "브이로그"),
                sectionTags = listOf(
                    Tag(1, "일상", true, 12),
                    Tag(2, "작업물", true, 8),
                    Tag(3, "공지", true, 3)
                ),
                selectedSectionTagId = 2,
                isLoadingSectionTags = false,
                onAddTag = {},
                onRemoveTag = {},
                onSelectSectionTag = {},
                onCreateSectionTag = {},
                onUpdateSectionTag = { _, _ -> },
                onDeleteSectionTag = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionTagTabContentPreview() {
    HaloTheme {
        Surface {
            SectionTagTabContent(
                sectionTags = listOf(
                    Tag(1, "일상", true, 12),
                    Tag(2, "작업물", true, 8),
                    Tag(3, "공지", true, 3)
                ),
                selectedSectionTagId = 2,
                isLoading = false,
                onSelectSectionTag = {},
                onCreateSectionTag = {},
                onUpdateSectionTag = { _, _ -> },
                onDeleteSectionTag = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionTagItemEditingPreview() {
    HaloTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                // 선택된 아이템
                SectionTagItem(
                    tag = Tag(1, "일상", true, 12),
                    isSelected = true,
                    isEditing = false,
                    editingValue = "",
                    onSelect = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onEditValueChange = {},
                    onEditConfirm = {}
                )
                // 수정 중인 아이템
                SectionTagItem(
                    tag = Tag(2, "작업물", true, 8),
                    isSelected = false,
                    isEditing = true,
                    editingValue = "작업물",
                    onSelect = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onEditValueChange = {},
                    onEditConfirm = {}
                )
                // 일반 아이템
                SectionTagItem(
                    tag = Tag(3, "공지", true, 3),
                    isSelected = false,
                    isEditing = false,
                    editingValue = "",
                    onSelect = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onEditValueChange = {},
                    onEditConfirm = {}
                )
            }
        }
    }
}
