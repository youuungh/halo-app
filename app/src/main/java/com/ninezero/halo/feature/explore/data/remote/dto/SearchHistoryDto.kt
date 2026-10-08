package com.ninezero.halo.feature.explore.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.explore.domain.model.SearchHistory
import kotlinx.serialization.Serializable

@Serializable
data class SearchHistoryDto(
    val id: Int,
    val keyword: String,
    val searchType: String,
    val resultCount: Int = 0,
    val createdAt: String
)

typealias SearchHistoryListResponseDto = PagedDto<SearchHistoryDto>

@Serializable
data class CreatorSearchDto(
    val id: Int,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val avatarThumbUrl: String? = null,
    val bio: String? = null,
    val role: String,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val productCount: Int = 0,
    val isFollowing: Boolean = false
)

typealias CreatorSearchListResponseDto = PagedDto<CreatorSearchDto>

fun SearchHistoryDto.toDomain(): SearchHistory = SearchHistory(
    id = id,
    keyword = keyword
)

fun CreatorSearchDto.toDomain(): Creator = Creator(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    avatarThumbUrl = avatarThumbUrl,
    role = role,
    isFollowing = isFollowing
)
