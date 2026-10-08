package com.ninezero.halo.core.ui.components.media

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.nextImageUrlsAfter

/** 다음 포스트 이미지 프리페치 */
@Composable
fun PrefetchUpcomingPostImages(
    listState: LazyListState,
    posts: List<Post>,
    aheadCount: Int = 4,
    keyToPostId: (Any?) -> Int?
) {
    val context = LocalContext.current
    val lastVisiblePostId by remember(keyToPostId) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { keyToPostId(it.key) }
                .lastOrNull()
        }
    }
    LaunchedEffect(lastVisiblePostId, posts) {
        posts.nextImageUrlsAfter(lastVisiblePostId, aheadCount).forEach { url ->
            context.imageLoader.enqueue(ImageRequest.Builder(context).data(url).build())
        }
    }
}
