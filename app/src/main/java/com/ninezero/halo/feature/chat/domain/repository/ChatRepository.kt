package com.ninezero.halo.feature.chat.domain.repository

import com.ninezero.halo.core.model.UploadTicket
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.ChatImageUpload
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageListResult
import com.ninezero.halo.feature.chat.domain.model.MessageSearchResult
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

// observe*는 로컬 DB 관찰, sync*는 서버 동기화
/** 채팅방·메시지 저장소 */
interface ChatRepository {
    /** 보관 안 된 채팅방 관찰 */
    fun observeChatRooms(): Flow<List<Chat>>
    /** 채팅방 관찰 */
    fun observeChatRoom(roomId: Int): Flow<Chat?>
    /** 채팅방 메시지 관찰 */
    fun observeMessages(roomId: Int): Flow<List<Message>>
    /** 캐시 메시지 조회 */
    suspend fun getCachedMessages(roomId: Int, limit: Int): List<Message>
    /** 채팅방 목록 서버 동기화 */
    suspend fun syncChatRooms(): Result<Unit>
    /** 최근 메시지 서버 동기화 */
    suspend fun syncRecentMessages(roomId: Int, limit: Int): Result<Unit>
    /** 실시간 채팅 이벤트 캐시 반영 */
    suspend fun applyRealtimeEvents()  // 수집하는 동안 계속

    // 채팅방
    /** 채팅방 목록 조회 */
    suspend fun getChatRooms(page: Int, limit: Int, includeArchived: Boolean = false): Result<List<Chat>>
    /** 채팅방 조회 */
    suspend fun getChatRoom(roomId: Int): Result<Chat>
    /** 채팅방 조회 또는 생성 */
    suspend fun createOrGetChatRoom(otherUserId: Int): Result<Chat>
    /** 상대와의 채팅방 조회 */
    suspend fun findChatRoomWithUser(otherUserId: Int): Result<Chat?>
    /** 채팅방 보관/해제 */
    suspend fun toggleArchive(roomId: Int, archived: Boolean): Result<Unit>
    /** 채팅방 삭제 */
    suspend fun clearChatRoom(roomId: Int): Result<Unit>

    // 메시지
    /** 메시지 목록 조회 */
    suspend fun getMessages(roomId: Int, limit: Int, beforeMessageId: Int? = null): Result<MessageListResult>
    /** 메시지 전송 */
    suspend fun sendMessage(
        roomId: Int,
        content: String?,
        messageType: ChatMessageType,
        mediaAttachments: List<String>? = null,
        mediaThumbnails: List<String>? = null,
        productId: Int? = null,
        thumbnailUrl: String? = null,
        duration: Long? = null,
        mediaWidth: Int? = null,
        mediaHeight: Int? = null,
        fileSize: Long? = null,
        fileName: String? = null,
        clientTempId: String? = null,
        uploadId: Int? = null
    ): Result<Message>
    /** 실패 메시지 재전송 */
    suspend fun retryFailedMessage(clientTempId: String): Result<Message>
    /** 실패 메시지 삭제 */
    suspend fun cancelFailedMessage(clientTempId: String)
    /** 앱 종료로 전송 중에 멈춘 텍스트 메시지를 실패로 전환 */
    suspend fun failStuckTextMessages(roomId: Int)
    /** 메시지 읽음 처리 */
    suspend fun markMessagesAsRead(roomId: Int): Result<Int?>
    /** 메시지 삭제 */
    suspend fun deleteMessage(messageId: Int): Result<Unit>
    /** 메시지 검색 */
    suspend fun searchMessages(roomId: Int, query: String, page: Int, limit: Int): Result<MessageSearchResult>
    /** 메시지 캐시 저장 */
    suspend fun cacheMessages(messages: List<Message>)
    /** 최신 페이지 캐시 저장 */
    suspend fun cacheLatestMessages(roomId: Int, messages: List<Message>)  // 그보다 오래된 이 방의 보낸 메시지 캐시는 삭제

    // 낙관적 갱신
    /** 이미지 메시지 임시 저장 */
    suspend fun insertImageMessage(
        roomId: Int,
        localUris: List<String>,
        clientTempId: String,
        content: String? = null,
        mediaWidth: Int? = null,
        mediaHeight: Int? = null
    )
    /** 미디어 메시지 임시 저장 */
    suspend fun insertMediaMessage(
        roomId: Int,
        messageType: ChatMessageType,
        localUri: String,
        clientTempId: String,
        content: String? = null,
        fileName: String? = null,
        fileSize: Long? = null,
        duration: Long? = null,
        mediaWidth: Int? = null,
        mediaHeight: Int? = null
    )
    /** 업로드 진행률 갱신 */
    suspend fun updateUploadProgress(clientTempId: String, progress: Float)
    /** 미디어 메시지 전송 완료 표시 */
    suspend fun markMediaMessageSent(clientTempId: String)
    /** 미디어 메시지 실패 표시 */
    suspend fun markMediaMessageFailed(clientTempId: String)
    /** 미디어 메시지 전송 중 표시 */
    suspend fun markMediaMessageSending(clientTempId: String)

    // 미디어 업로드
    /** 채팅 이미지 업로드 */
    suspend fun uploadImages(roomId: Int, images: List<MultipartBody.Part>): Result<ChatImageUpload>
    /** 채팅 영상·파일 업로드 준비 */
    suspend fun prepareUpload(
        roomId: Int,
        messageType: ChatMessageType,
        mimeType: String,
        size: Long,
        fileName: String?
    ): Result<UploadTicket>
}
