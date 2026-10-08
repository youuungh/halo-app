package com.ninezero.halo.feature.chat.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.dto.UploadPrepareDto
import com.ninezero.halo.feature.chat.data.remote.dto.ChatDto
import com.ninezero.halo.feature.chat.data.remote.dto.ChatRequest
import com.ninezero.halo.feature.chat.data.remote.dto.ChatRoomListResponseDto
import com.ninezero.halo.feature.chat.data.remote.dto.ChatUploadPrepareRequest
import com.ninezero.halo.feature.chat.data.remote.dto.MarkMessagesReadResponseDto
import com.ninezero.halo.feature.chat.data.remote.dto.MessageDto
import com.ninezero.halo.feature.chat.data.remote.dto.MessageListResponseDto
import com.ninezero.halo.feature.chat.data.remote.dto.PaginatedMessageResponseDto
import com.ninezero.halo.feature.chat.data.remote.dto.SendMessageRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {
    /** 내 채팅방 목록 조회 */
    @GET("api/v1/chats")
    suspend fun getChatRooms(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("includeArchived") includeArchived: Boolean = false
    ): Response<ApiResponse<ChatRoomListResponseDto>>

    /** 채팅방 조회 또는 생성 */
    @POST("api/v1/chats")
    suspend fun createChatRoom(
        @Body request: ChatRequest
    ): Response<ApiResponse<ChatDto>>

    /** 상대와의 채팅방 조회 */
    @GET("api/v1/chats/with/{userId}")
    suspend fun findChatRoomWithUser(
        @Path("userId") userId: Int
    ): Response<ApiResponse<ChatDto>>

    /** 채팅방 상세 조회 */
    @GET("api/v1/chats/{roomId}")
    suspend fun getChatRoom(
        @Path("roomId") roomId: Int
    ): Response<ApiResponse<ChatDto>>

    /** 채팅방 보관/해제 */
    @PUT("api/v1/chats/{roomId}/archive")
    suspend fun toggleArchive(
        @Path("roomId") roomId: Int,
        @Query("archived") archived: Boolean = true
    ): Response<ApiResponse<Map<String, String>>>

    /** 채팅방 삭제 */
    @PUT("api/v1/chats/{roomId}/clear")
    suspend fun clearChatRoom(
        @Path("roomId") roomId: Int
    ): Response<ApiResponse<Map<String, String>>>

    /** 메시지 목록 조회 */
    @GET("api/v1/chats/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: Int,
        @Query("limit") limit: Int = 50,
        @Query("beforeMessageId") beforeMessageId: Int? = null
    ): Response<ApiResponse<MessageListResponseDto>>

    /** 메시지 전송 */
    @POST("api/v1/chats/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: Int,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>

    /** 메시지 읽음 처리 */
    @PUT("api/v1/chats/{roomId}/messages/read")
    suspend fun markMessagesAsRead(
        @Path("roomId") roomId: Int
    ): Response<ApiResponse<MarkMessagesReadResponseDto>>

    /** 메시지 검색 */
    @GET("api/v1/chats/{roomId}/messages/search")
    suspend fun searchMessages(
        @Path("roomId") roomId: Int,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PaginatedMessageResponseDto>>

    /** 채팅 이미지 업로드 */
    @Multipart
    @POST("api/v1/chats/{roomId}/images")
    suspend fun uploadImages(
        @Path("roomId") roomId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<ApiResponse<Map<String, List<String>>>>

    /** 채팅 영상·파일 업로드 준비 */
    @POST("api/v1/chats/{roomId}/uploads")
    suspend fun prepareUpload(
        @Path("roomId") roomId: Int,
        @Body request: ChatUploadPrepareRequest
    ): Response<ApiResponse<UploadPrepareDto>>

    /** 메시지 삭제 */
    @DELETE("api/v1/chats/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Int
    ): Response<Unit>
}
