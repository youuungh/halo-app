package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState

private val ControlSize = 40.dp

/** 탭 캡슐, 누르면 검색창으로 바뀜 */
@Composable
fun FollowTabSearchRow(
    isSearchMode: Boolean,
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleSearch: () -> Unit,
    focusRequester: FocusRequester,
    hazeState: HazeState,
    hazeBackgroundColor: Color,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(ControlSize)
                .clip(CircleShape)
                .haloBlur(hazeState, HaloBlurs.glassField(hazeBackgroundColor))
        ) {
            AnimatedContent(
                targetState = isSearchMode,
                transitionSpec = { fadeIn(tween(200, delayMillis = 80)) togetherWith fadeOut(tween(120)) },
                label = "follow_tab_search"
            ) { searching ->
                if (searching) {
                    SearchField(
                        query = query,
                        placeholder = placeholder,
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        focusRequester = focusRequester
                    )
                } else {
                    tabs()
                }
            }
        }

        Box(
            modifier = Modifier
                .size(ControlSize)
                .clip(CircleShape)
                .haloBlur(hazeState, HaloBlurs.glassField(hazeBackgroundColor))
                .clickable(onClick = onToggleSearch),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isSearchMode, label = "follow_search_icon") { searching ->
                Icon(
                    painter = painterResource(if (searching) R.drawable.ic_close else R.drawable.ic_search),
                    contentDescription = if (searching) "검색 닫기" else "검색",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // 열 때만 포커스, 돌아왔을 땐 키보드 안 띄움
    var wasSearchMode by rememberSaveable { mutableStateOf(isSearchMode) }
    LaunchedEffect(isSearchMode) {
        if (isSearchMode && !wasSearchMode) focusRequester.requestFocus()
        wasSearchMode = isSearchMode
    }
}

@Composable
private fun SearchField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() })
            )
        }
    }
}
