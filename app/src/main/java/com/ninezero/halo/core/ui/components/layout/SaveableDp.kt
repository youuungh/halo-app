package com.ninezero.halo.core.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 복귀에도 보존되는 Dp 상태 */
@Composable
fun rememberSaveableDp(initial: Dp = 0.dp): MutableState<Dp> =
    rememberSaveable(stateSaver = Saver({ it.value }, { it.dp })) { mutableStateOf(initial) }
