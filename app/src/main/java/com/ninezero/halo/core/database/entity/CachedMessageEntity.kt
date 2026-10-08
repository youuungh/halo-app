package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_messages",
    indices = [
        Index(value = ["roomId", "createdAt"]),
        Index(value = ["roomId", "id"]),
        Index(value = ["clientTempId"])
    ]
)
data class CachedMessageEntity(
    @PrimaryKey
    val id: Int,
    val roomId: Int,
    // 보낸 사람 정보
    val senderId: Int,
    val senderUsername: String,
    val senderDisplayName: String?,
    val senderAvatarUrl: String?,
    // 메시지 데이터
    val chatMessageType: String,
    val content: String?,
    val mediaAttachments: String?,          // JSON array
    val mediaThumbnails: String? = null,    // JSON array, 썸네일
    val thumbnailUrl: String? = null,       // 비디오 포스터 썸네일
    val duration: Long? = null,             // 비디오 길이 ms
    val mediaWidth: Int? = null,            // 원본 너비, 로드 전 비율 예약
    val mediaHeight: Int? = null,           // 단일 이미지/비디오 원본 높이
    val fileSize: Long? = null,             // 파일 크기 bytes
    val fileName: String? = null,
    // 상품 정보
    val productId: Int?,
    val productName: String?,
    val productPrice: String?,
    val productOriginalPrice: String? = null,
    val productDiscountRate: Int? = null,
    val productImageUrl: String?,
    val productCanAccess: Boolean?,
    val productCreatorId: Int? = null,
    val productCreatorName: String? = null,
    val productRequiredTier: String? = null,
    // 포스트 정보
    val postId: Int? = null,
    val postContent: String? = null,
    val postAuthorName: String? = null,
    val postAuthorAvatarUrl: String? = null,
    val postCanAccess: Boolean? = null,
    val postMediaAttachments: String? = null,   // JSON array
    val postMediaType: String? = null,
    val postThumbnailUrl: String? = null,
    val postMediaThumbnails: String? = null,    // JSON array, 타일별 썸네일
    val postMediaTypes: String? = null,         // JSON array, 타일별 IMAGE·VIDEO
    val postLikeCount: Int? = null,
    val postCommentCount: Int? = null,
    val postShareCount: Int? = null,
    val postCreatorId: Int? = null,
    val postRequiredTier: String? = null,
    val postIsHidden: Boolean? = null,          // 크리에이터 해제로 숨겨진 글
    // 상태
    val isRead: Boolean,
    val readAt: Long?,
    val createdAt: Long,
    val isFromMe: Boolean,
    val isDeleted: Boolean = false,
    val status: String = "SENT",                // SENT / SENDING / FAILED
    val clientTempId: String? = null,           // 임시 id, 서버 응답 시 교체
    val uploadProgress: Float = 1f,             // 업로드 진행률 0~1
    val cachedAt: Long = System.currentTimeMillis()
)
