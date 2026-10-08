package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.picker.WheelPicker
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** 날짜 선택 시트 */
@Composable
fun DatePickerBottomSheet(
    initialDate: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDateTime? = null,
    title: String = "날짜 선택",
    includeTime: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedDate by remember { mutableStateOf(initialDate.toLocalDate()) }
    var selectedHour by remember { mutableIntStateOf(initialDate.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialDate.minute) }
    val minLocalDate = remember(minDate) { minDate?.toLocalDate() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier
    ) {
        DatePickerBottomSheetContent(
            title = title,
            selectedDate = selectedDate,
            minDate = minLocalDate,
            includeTime = includeTime,
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            onDateSelected = { selectedDate = it },
            onHourSelected = { selectedHour = it },
            onMinuteSelected = { selectedMinute = it },
            onConfirm = {
                val result = if (includeTime) {
                    selectedDate.atTime(selectedHour, selectedMinute)
                } else {
                    selectedDate.atTime(initialDate.hour, initialDate.minute, initialDate.second)
                }
                onConfirm(result)
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun DatePickerBottomSheetContent(
    title: String,
    selectedDate: LocalDate,
    minDate: LocalDate?,
    includeTime: Boolean,
    selectedHour: Int,
    selectedMinute: Int,
    onDateSelected: (LocalDate) -> Unit,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    var headerHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 캘린더 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(hazeState)
                .padding(top = headerHeight)
                .navigationBarsPadding()
        ) {
            CustomCalendar(
                selectedDate = selectedDate,
                minDate = minDate,
                onDateSelected = onDateSelected
            )

            if (includeTime) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                TimeWheelSection(
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onHourSelected = onHourSelected,
                    onMinuteSelected = onMinuteSelected
                )
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
                    detectTapGestures { }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Text(
                    text = title,
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
        }
    }
}

@Composable
private fun CustomCalendar(
    selectedDate: LocalDate,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var isWheelPickerMode by remember { mutableStateOf(false) }

    BackHandler(enabled = isWheelPickerMode) {
        isWheelPickerMode = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AnimatedContent(
            targetState = isWheelPickerMode,
            transitionSpec = {
                if (targetState) {
                    // 캘린더→휠: 캘린더는 위로, 휠은 아래에서
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith
                            (fadeOut() + slideOutVertically { -it / 2 }) using
                            SizeTransform(clip = false)
                } else {
                    // 휠→캘린더: 휠은 아래로, 캘린더는 위에서
                    (fadeIn() + slideInVertically { -it / 2 }) togetherWith
                            (fadeOut() + slideOutVertically { it / 2 }) using
                            SizeTransform(clip = false)
                }
            },
            label = "CalendarWheelPickerTransition"
        ) { wheelMode ->
            if (wheelMode) {
                DateWheelPicker(
                    selectedDate = selectedDate,
                    minDate = minDate,
                    onDateChanged = { newDate ->
                        onDateSelected(newDate)
                        displayedYearMonth = YearMonth.from(newDate)
                    },
                    onBackToCalendar = { isWheelPickerMode = false }
                )
            } else {
                Column {
                    CalendarMonthNavigation(
                        yearMonth = displayedYearMonth,
                        onPreviousMonth = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                        onNextMonth = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                        onMonthTextClick = { isWheelPickerMode = true }
                    )

                    CalendarDayOfWeekHeader()

                    AnimatedContent(
                        targetState = displayedYearMonth,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            slideInHorizontally { fullWidth -> direction * fullWidth } togetherWith
                                    slideOutHorizontally { fullWidth -> -direction * fullWidth } using
                                    SizeTransform(clip = false)
                        },
                        label = "CalendarMonthTransition"
                    ) { yearMonth ->
                        CalendarGrid(
                            yearMonth = yearMonth,
                            selectedDate = selectedDate,
                            minDate = minDate,
                            onDateSelected = onDateSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthNavigation(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthText = "${yearMonth.year}년 ${yearMonth.monthValue}월"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(36.dp)
                .bounceClick()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = "이전 월",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .bounceClick()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMonthTextClick)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = monthText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down_small),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(36.dp)
                .bounceClick()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = "다음 월",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CalendarDayOfWeekHeader(
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val color = when (index) {
                0 -> MaterialTheme.colorScheme.error                // 일요일 빨강
                6 -> MaterialTheme.colorScheme.primary              // 토요일 파랑
                else -> MaterialTheme.colorScheme.onSurfaceVariant  // 평일
            }
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // 일=0, 월=1, ..., 토=6
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        var dayCounter = 1
        val totalCells = startDayOfWeek + daysInMonth
        val totalRows = (totalCells + 6) / 7

        repeat(totalRows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isDisabled = minDate != null && date.isBefore(minDate)
                        val isSunday = col == 0
                        val isSaturday = col == 6

                        CalendarDayCell(
                            day = dayCounter,
                            isSelected = isSelected,
                            isToday = isToday,
                            isDisabled = isDisabled,
                            isSunday = isSunday,
                            isSaturday = isSaturday,
                            onClick = { if (!isDisabled) onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = when {
        isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        isSunday -> MaterialTheme.colorScheme.error
        isSaturday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .then(
                if (!isDisabled) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun DateWheelPicker(
    selectedDate: LocalDate,
    minDate: LocalDate?,
    onDateChanged: (LocalDate) -> Unit,
    onBackToCalendar: () -> Unit
) {
    val currentYear = LocalDate.now().year
    val startYear = minDate?.year ?: (currentYear - 50)
    val endYear = currentYear + 10
    val years = (startYear..endYear).toList()
    val months = (1..12).toList()

    var selectedYear by remember { mutableIntStateOf(selectedDate.year) }
    var selectedMonth by remember { mutableIntStateOf(selectedDate.monthValue) }
    var selectedDay by remember { mutableIntStateOf(selectedDate.dayOfMonth) }

    val daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    val days = (1..daysInMonth).toList()

    // 월/년 변경으로 일수가 줄면 일을 클램프
    val clampedDay = selectedDay.coerceAtMost(daysInMonth)
    if (clampedDay != selectedDay) {
        selectedDay = clampedDay
    }

    // minDate 제약 강제
    fun buildDate(year: Int, month: Int, day: Int): LocalDate {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        val safeDay = day.coerceAtMost(maxDay)
        val date = LocalDate.of(year, month, safeDay)
        return if (minDate != null && date.isBefore(minDate)) minDate else date
    }

    val yearIndex = years.indexOf(selectedYear).coerceAtLeast(0)
    val monthIndex = months.indexOf(selectedMonth).coerceAtLeast(0)
    val dayIndex = days.indexOf(clampedDay).coerceAtLeast(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 탭하면 캘린더로 돌아가는 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .bounceClick()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBackToCalendar)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${selectedYear}년 ${selectedMonth}월 ${clampedDay}일",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_up_small),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(
                items = years.map { "${it}년" },
                selectedIndex = yearIndex,
                onSelectedChanged = { index ->
                    selectedYear = years[index]
                    val newDate = buildDate(selectedYear, selectedMonth, selectedDay)
                    selectedYear = newDate.year
                    selectedMonth = newDate.monthValue
                    selectedDay = newDate.dayOfMonth
                    onDateChanged(newDate)
                },
                modifier = Modifier.weight(1f)
            )

            WheelPicker(
                items = months.map { "${it}월" },
                selectedIndex = monthIndex,
                onSelectedChanged = { index ->
                    selectedMonth = months[index]
                    val newDate = buildDate(selectedYear, selectedMonth, selectedDay)
                    selectedYear = newDate.year
                    selectedMonth = newDate.monthValue
                    selectedDay = newDate.dayOfMonth
                    onDateChanged(newDate)
                },
                modifier = Modifier.weight(1f)
            )

            WheelPicker(
                items = days.map { "${it}일" },
                selectedIndex = dayIndex,
                onSelectedChanged = { index ->
                    selectedDay = days[index]
                    val newDate = buildDate(selectedYear, selectedMonth, selectedDay)
                    selectedYear = newDate.year
                    selectedMonth = newDate.monthValue
                    selectedDay = newDate.dayOfMonth
                    onDateChanged(newDate)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TimeWheelSection(
    selectedHour: Int,
    selectedMinute: Int,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = hours.map { "%02d".format(it) },
            selectedIndex = selectedHour.coerceIn(0, 23),
            onSelectedChanged = { onHourSelected(hours[it]) },
            visibleItemCount = 3,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        WheelPicker(
            items = minutes.map { "%02d".format(it) },
            selectedIndex = selectedMinute.coerceIn(0, 59),
            onSelectedChanged = { onMinuteSelected(minutes[it]) },
            visibleItemCount = 3,
            modifier = Modifier.weight(1f)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun DatePickerBottomSheetPreview() {
    HaloTheme {
        Surface {
            DatePickerBottomSheetContent(
                title = "날짜 선택",
                selectedDate = LocalDate.now(),
                minDate = null,
                includeTime = false,
                selectedHour = 0,
                selectedMinute = 0,
                onDateSelected = {},
                onHourSelected = {},
                onMinuteSelected = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun DatePickerBottomSheetWithMinDatePreview() {
    HaloTheme {
        Surface {
            DatePickerBottomSheetContent(
                title = "종료일 선택",
                selectedDate = LocalDate.now().plusDays(7),
                minDate = LocalDate.now(),
                includeTime = true,
                selectedHour = 23,
                selectedMinute = 59,
                onDateSelected = {},
                onHourSelected = {},
                onMinuteSelected = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
