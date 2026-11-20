package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.DarkExpandText
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.feature.store.domain.model.DetailContentBlock
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.components.richTextAlign
import com.ninezero.halo.feature.store.presentation.components.richTextColorOrNull
import com.ninezero.halo.feature.store.presentation.components.richTextDecoration
import com.ninezero.halo.feature.store.presentation.components.richTextStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private val DetailFadeHeight = 140.dp
private const val DETAIL_FADE_SOLID_FRACTION = 0.55f  // 버튼 윗선에서 바탕색이 됨
private val DetailExpandButtonShape = RoundedCornerShape(24.dp)

/** 상품 상세 섹션 제목 */
@Composable
internal fun ProductDetailSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (count != null) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductDescriptionSection(
    description: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasTextOverflow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ProductDetailSectionTitle(text = "상품 정보")

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) {
                    hasTextOverflow = result.hasVisualOverflow
                }
            }
        )

        if (hasTextOverflow || isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onToggleExpanded,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 1.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isExpanded) "상품정보 접기" else "상품정보 펼쳐보기",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_chevron_up_small
                        else R.drawable.ic_chevron_down_small
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DetailContentSection(
    detailContent: List<DetailContentBlock>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val collapsedMaxHeight = 600.dp
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .then(
                    if (isExpanded) Modifier
                    else Modifier
                        .height(collapsedMaxHeight)
                        .clipToBounds()
                )
        ) {
            Box(
                modifier = Modifier
                    .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.matchParentSize())
                    .hazeSource(hazeState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isExpanded) Modifier
                            else Modifier.wrapContentHeight(align = Alignment.Top, unbounded = true)
                        )
                ) {
                    DetailContentBlocks(detailContent)
                }
            }

            if (!isExpanded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(DetailFadeHeight)
                        .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor, end = 1f, easing = LinearEasing))
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                DETAIL_FADE_SOLID_FRACTION to backgroundColor
                            )
                        )
                )
                DetailExpandToggleButton(
                    isExpanded = false,
                    onClick = onToggleExpanded,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailExpandToggleButton(
                isExpanded = true,
                onClick = onToggleExpanded
            )
        }
    }
}

@Composable
private fun DetailExpandToggleButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background

    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 12.dp)
            .bounceClick()
            .fillMaxWidth()
            .height(48.dp),
        shape = DetailExpandButtonShape,
        color = primary.copy(alpha = if (isDark) 0.18f else 0.1f).compositeOver(background),
        contentColor = if (isDark) DarkExpandText else primary
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExpanded) "간략히 보기" else "자세히 보기",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(
                    if (isExpanded) R.drawable.ic_chevron_up
                    else R.drawable.ic_chevron_down
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DetailContentBlocks(blocks: List<DetailContentBlock>) {
    val renderBlocks = remember(blocks) { buildDetailRenderBlocks(blocks) }
    var numberedIndex = 0

    renderBlocks.forEachIndexed { index, renderBlock ->
        when (renderBlock) {
            is DetailRenderBlock.Image -> {
                // 크기 있으면 aspectRatio 즉시 확정
                val imageModifier = Modifier
                    .fillMaxWidth()
                    .let { baseModifier ->
                        if (renderBlock.width != null && renderBlock.height != null &&
                            renderBlock.width > 0 && renderBlock.height > 0
                        ) {
                            baseModifier.aspectRatio(renderBlock.width.toFloat() / renderBlock.height.toFloat())
                        } else baseModifier
                    }
                    .clip(MaterialTheme.shapes.small)
                AsyncImage(
                    model = renderBlock.url,
                    contentDescription = "상세 이미지",
                    modifier = imageModifier,
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                    placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                )
                numberedIndex = 0
                if (index < renderBlocks.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            is DetailRenderBlock.TextBlock -> {
                if (renderBlock.textStyle == RichTextBlockStyle.NUMBERED) numberedIndex += 1
                else numberedIndex = 0

                DetailTextBlockItem(
                    block = renderBlock,
                    numberedIndex = numberedIndex.takeIf { renderBlock.textStyle == RichTextBlockStyle.NUMBERED }
                )
                if (index < renderBlocks.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailTextBlockItem(
    block: DetailRenderBlock.TextBlock,
    numberedIndex: Int?
) {
    when (block.textStyle) {
        RichTextBlockStyle.QUOTE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 10.dp)
                        .width(3.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                )
                Text(
                    text = block.text,
                    style = richTextStyle(block.textStyle),
                    textAlign = richTextAlign(block.textAlignment),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        RichTextBlockStyle.BULLET,
        RichTextBlockStyle.NUMBERED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (block.textStyle == RichTextBlockStyle.BULLET) "\u2022" else "${numberedIndex ?: 1}.",
                    style = richTextStyle(block.textStyle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = block.text,
                    style = richTextStyle(block.textStyle),
                    textAlign = richTextAlign(block.textAlignment),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        else -> {
            Text(
                text = block.text,
                style = richTextStyle(block.textStyle),
                textAlign = richTextAlign(block.textAlignment),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private sealed interface DetailRenderBlock {
    data class TextBlock(
        val blockId: String?,
        val text: AnnotatedString,
        val textStyle: RichTextBlockStyle,
        val textAlignment: RichTextAlignment
    ) : DetailRenderBlock

    data class Image(
        val url: String,
        val width: Int?,
        val height: Int?
    ) : DetailRenderBlock
}

private fun buildDetailRenderBlocks(blocks: List<DetailContentBlock>): List<DetailRenderBlock> {
    val result = mutableListOf<DetailRenderBlock>()
    val currentSegments = mutableListOf<DetailContentBlock>()
    var currentBlockId: String? = null
    var currentTextStyle = RichTextBlockStyle.PARAGRAPH
    var currentTextAlignment = RichTextAlignment.START

    fun flushText() {
        if (currentSegments.isEmpty()) return

        val annotated = buildAnnotatedString {
            currentSegments.forEach { segment ->
                val content = segment.content ?: return@forEach
                withStyle(
                    SpanStyle(
                        fontWeight = if (segment.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (segment.italic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = richTextDecoration(
                            segment.underline,
                            segment.strikethrough
                        ),
                        color = richTextColorOrNull(segment.textColor) ?: Color.Unspecified
                    )
                ) {
                    append(content)
                }
            }
        }

        result.add(
            DetailRenderBlock.TextBlock(
                blockId = currentBlockId,
                text = annotated,
                textStyle = currentTextStyle,
                textAlignment = currentTextAlignment
            )
        )
        currentSegments.clear()
        currentBlockId = null
        currentTextStyle = RichTextBlockStyle.PARAGRAPH
        currentTextAlignment = RichTextAlignment.START
    }

    blocks.forEach { block ->
        when (block.type) {
            "image" -> {
                flushText()
                // 빈 url은 렌더 안 함
                block.url?.takeIf { it.isNotBlank() }?.let {
                    result.add(
                        DetailRenderBlock.Image(
                            url = it,
                            width = block.width,
                            height = block.height
                        )
                    )
                }
            }

            "text" -> {
                val shouldFlush = currentSegments.isNotEmpty() && (
                        (block.blockId != null && currentBlockId != null && block.blockId != currentBlockId) ||
                                block.textStyle != currentTextStyle ||
                                block.textAlignment != currentTextAlignment
                        )

                if (shouldFlush) flushText()
                if (currentSegments.isEmpty()) {
                    currentBlockId = block.blockId
                    currentTextStyle = block.textStyle
                    currentTextAlignment = block.textAlignment
                }
                currentSegments.add(block)
            }
        }
    }

    flushText()
    return result
}

private const val PREVIEW_DESCRIPTION =
    "최고급 사운드 품질의 무선 이어폰입니다.\n노이즈 캔슬링 기능과 30시간 배터리를 지원합니다.\n" +
            "인체공학적 설계로 편안한 착용감을 제공합니다.\n최신 블루투스 5.3 기술을 적용해 안정적으로 연결됩니다.\n" +
            "IPX5 방수 등급으로 운동 시에도 안심하고 사용할 수 있습니다.\n충전 케이스는 USB-C 고속 충전을 지원합니다."

@HaloLightDarkPreview
@Composable
private fun ProductDescriptionSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductDescriptionSection(
                    description = PREVIEW_DESCRIPTION,
                    isExpanded = false,
                    onToggleExpanded = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDescriptionSectionExpandedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductDescriptionSection(
                    description = PREVIEW_DESCRIPTION,
                    isExpanded = true,
                    onToggleExpanded = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun DetailContentSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                DetailContentSection(
                    detailContent = listOf(
                        DetailContentBlock(type = "text", content = "제품 상세 안내입니다."),
                        DetailContentBlock(
                            type = "image",
                            url = "https://picsum.photos/600/400",
                            width = 600,
                            height = 400
                        ),
                        DetailContentBlock(type = "text", content = "구성품: 본체, 케이스, 케이블")
                    ),
                    isExpanded = true,
                    onToggleExpanded = {}
                )
            }
        }
    }
}
