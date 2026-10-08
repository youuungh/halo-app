package com.ninezero.halo

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.dto.CommentDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import com.ninezero.halo.feature.tag.data.remote.dto.TagDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ApiContractMismatchTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun postListWrapper_cannotBeDecoded_asLegacyListContract() {
        val payload = """
            {
              "success": true,
              "data": {
                "items": [
                  {
                    "id": 1,
                    "content": "post",
                    "author": {
                      "id": 7,
                      "username": "tester"
                    },
                    "createdAt": "2026-03-27T12:00:00",
                    "updatedAt": "2026-03-27T12:00:00"
                  }
                ],
                "totalCount": 1,
                "page": 1,
                "totalPages": 1,
                "hasNext": false,
                "hasPrevious": false
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<List<PostDto>>>(payload)
        }
    }

    @Test
    fun creatorProfileResponse_cannotBeDecoded_asPostListResponse() {
        val payload = """
            {
              "success": true,
              "data": {
                "pinnedPosts": [],
                "tagSections": [],
                "allPosts": {
                  "items": [],
                  "totalCount": 0,
                  "page": 1,
                  "totalPages": 1,
                  "hasNext": false,
                  "hasPrevious": false
                }
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<com.ninezero.halo.feature.feed.data.remote.dto.PostListResponse>>(payload)
        }
    }

    @Test
    fun commentListWrapper_cannotBeDecoded_asLegacyListContract() {
        val payload = """
            {
              "success": true,
              "data": {
                "items": [
                  {
                    "id": 10,
                    "postId": 1,
                    "content": "comment",
                    "author": {
                      "id": 7,
                      "username": "tester"
                    },
                    "createdAt": "2026-03-27T12:00:00",
                    "updatedAt": "2026-03-27T12:00:00"
                  }
                ],
                "totalCount": 1,
                "page": 1,
                "totalPages": 1,
                "hasNext": false,
                "hasPrevious": false
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<List<CommentDto>>>(payload)
        }
    }

    @Test
    fun tagWrapper_cannotBeDecoded_asLegacyListContract() {
        val payload = """
            {
              "success": true,
              "data": {
                "tags": [
                  {
                    "id": 3,
                    "name": "news",
                    "targetType": "POST",
                    "isSectionEnabled": true,
                    "createdAt": "2026-03-27T12:00:00"
                  }
                ]
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<List<TagDto>>>(payload)
        }
    }

    @Test
    fun likedUsersWrapper_cannotBeDecoded_asLegacyUserListContract() {
        val payload = """
            {
              "success": true,
              "data": {
                "items": [
                  {
                    "user": {
                      "id": 7,
                      "username": "tester",
                      "role": "USER"
                    },
                    "likedAt": "2026-03-27T12:00:00"
                  }
                ],
                "totalCount": 1,
                "page": 1,
                "totalPages": 1,
                "hasNext": false,
                "hasPrevious": false
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<List<UserSummaryDto>>>(payload)
        }
    }

    @Test
    fun likedPostsMap_cannotBeDecoded_asPostListContract() {
        val payload = """
            {
              "success": true,
              "data": {
                "postIds": [1, 2, 3]
              }
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ApiResponse<List<PostDto>>>(payload)
        }
    }

    @Test
    fun successfulResponse_withoutBody_isHandledAsFailure() {
        @Suppress("UNCHECKED_CAST")
        val bodylessSuccess = Response.success(null) as Response<ApiResponse<Unit>>

        val result = runBlocking {
            safeApiCall<Unit> { bodylessSuccess }
        }

        assertTrue(result.isFailure)
    }
}
