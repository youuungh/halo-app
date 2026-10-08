package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ninezero.halo.core.ui.components.media.rememberAutoplayTarget
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData

/** 답글 탭 자동재생 대상 */
internal data class ThreadVideo(val key: String, val url: String)

internal fun postVideoKey(postId: Int) = "post_$postId"
internal fun commentVideoKey(commentId: Int) = "comment_$commentId"

/** 아이템의 자동재생 비디오 선택 */
internal fun CommentThreadData.primaryVideo(): ThreadVideo? {
    val isSingleMyComment = comments.size == 1 && comments.first().isProfileUser
    if (isSingleMyComment && !post.isDeleted) {
        post.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }?.let {
            return ThreadVideo(postVideoKey(post.id), it.url)
        }
    }
    comments.forEach { comment ->
        comment.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }?.let {
            return ThreadVideo(commentVideoKey(comment.id), it.url)
        }
    }
    return null
}

@Composable
internal fun rememberActiveVideoKey(  // 키 포맷은 LazyColumn key와 일치
    threads: List<CommentThreadData>,
    listState: LazyListState
): String? {
    val threadByKey = remember(threads) {
        threads.associateBy { "thread_${it.post.id}_${it.comments.lastOrNull()?.id ?: 0}" }
    }

    val activeVideoKey by rememberAutoplayTarget(listState) { item ->
        threadByKey[item.key as? String]?.primaryVideo()?.key
    }

    // 다음 스레드 영상 prefetch
    val context = LocalContext.current
    val orderedVideos = remember(threads) { threads.mapNotNull { it.primaryVideo() } }
    LaunchedEffect(activeVideoKey, orderedVideos) {
        val idx = orderedVideos.indexOfFirst { it.key == activeVideoKey }
        val start = if (idx >= 0) idx + 1 else 0
        VideoPreloader.prefetch(context, orderedVideos.drop(start).take(2).map { it.url })
    }

    return activeVideoKey
}
