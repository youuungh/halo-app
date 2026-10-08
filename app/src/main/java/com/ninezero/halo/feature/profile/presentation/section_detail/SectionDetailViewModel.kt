package com.ninezero.halo.feature.profile.presentation.section_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.feed.domain.model.CachedPostsTracker
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedPostsUseCase
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class SectionDetailViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val productRepository: ProductRepository,
    private val observeCachedPostsUseCase: ObserveCachedPostsUseCase
) : ViewModel(), OrbitContainerHost<SectionDetailState, SectionDetailState, SectionDetailSideEffect> {

    private val cachedPostsTracker = CachedPostsTracker()
    private var initialized = false
    private val listGeneration = ListGeneration()

    override val container: OrbitContainer<SectionDetailState, SectionDetailState, SectionDetailSideEffect> =
        orbitContainer(SectionDetailState())

    init {
        observeCachedPosts()
    }

    // 다른 화면 좋아요/댓글 변경 자동 반영
    private fun observeCachedPosts() {
        observeCachedPostsUseCase()
            .onEach { cachedPosts ->
                // 반영은 reduce 안에서, 동시 갱신 유실 방지
                val (cachedPostsMap, previousCachedIds) = cachedPostsTracker.track(cachedPosts)
                intent {
                    reduce {
                        state.copy(
                            posts = state.posts.syncWithCache(cachedPostsMap, previousCachedIds)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SectionDetailIntent) {
        when (intent) {
            is SectionDetailIntent.Initialize -> initialize(
                intent.sectionType,
                intent.creatorId,
                intent.tagId,
                intent.title
            )
            is SectionDetailIntent.LoadMore -> loadMore()
            is SectionDetailIntent.Refresh -> refresh()
            is SectionDetailIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is SectionDetailIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            is SectionDetailIntent.ShowRestrictedContentSnackbar -> showRestrictedSnackbar(intent.message)
            is SectionDetailIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun initialize(
        sectionType: SectionType,
        creatorId: Int,
        tagId: Int?,
        title: String
    ) = intent {
        // 화면 복귀 때마다 다시 불려도 받은 목록·스크롤 유지
        if (initialized) return@intent
        initialized = true
        reduce {
            state.copy(
                sectionType = sectionType,
                creatorId = creatorId,
                tagId = tagId,
                title = title,
                isLoading = true,
                posts = emptyList(),
                products = emptyList(),
                page = 1,
                hasMore = true,
                error = null
            )
        }
        loadData()
    }

    private fun loadData() = intent {
        when (state.sectionType) {
            SectionType.PINNED_POST -> loadPinnedPosts()
            SectionType.POST_TAG -> loadPostsByTag()
            SectionType.PRODUCT_TAG -> loadProductsByTag()
        }
    }

    private fun loadPinnedPosts() = intent {
        val generation = listGeneration.current()
        profileRepository.getPinnedPosts(state.creatorId)
            .onSuccess { (posts, _) ->
                if (listGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        posts = posts,
                        isLoading = false,
                        hasMore = false,  // 고정 포스트는 페이지네이션 없음
                        error = null
                    )
                }
            }
            .onFailure { error ->
                if (listGeneration.current() != generation) return@intent
                val message = error.message ?: "로드 실패"
                reduce {
                    state.copy(
                        isLoading = false,
                        page = (state.page - 1).coerceAtLeast(1),  // 실패한 페이지를 다음 더보기에서 다시 받음
                        error = message,
                        snackbarState = SnackbarState(message)
                    )
                }
            }
    }

    private fun loadPostsByTag() = intent {
        val tagId = state.tagId ?: return@intent
        val generation = listGeneration.current()
        val page = state.page  // loadMore가 올린 페이지, 세대와 함께 비교

        profileRepository.getPostsByTag(tagId, page)
            .onSuccess { (posts, hasMore) ->
                if (listGeneration.current() != generation || state.page != page) return@intent
                reduce {
                    state.copy(
                        posts = if (page == 1) posts else (state.posts + posts).distinctBy { it.id },
                        isLoading = false,
                        hasMore = hasMore,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                if (listGeneration.current() != generation || state.page != page) return@intent
                val message = error.message ?: "로드 실패"
                reduce {
                    state.copy(
                        isLoading = false,
                        page = (state.page - 1).coerceAtLeast(1),  // 실패한 페이지를 다음 더보기에서 다시 받음
                        error = message,
                        snackbarState = SnackbarState(message)
                    )
                }
            }
    }

    private fun loadProductsByTag() = intent {
        val tagId = state.tagId ?: return@intent
        val generation = listGeneration.current()
        val page = state.page

        productRepository.getProductsByTag(tagId, page)
            .onSuccess { (products, hasMore) ->
                if (listGeneration.current() != generation || state.page != page) return@intent
                reduce {
                    state.copy(
                        products = if (page == 1) products else (state.products + products).distinctBy { it.id },
                        isLoading = false,
                        hasMore = hasMore,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                if (listGeneration.current() != generation || state.page != page) return@intent
                val message = error.message ?: "로드 실패"
                reduce {
                    state.copy(
                        isLoading = false,
                        page = (state.page - 1).coerceAtLeast(1),  // 실패한 페이지를 다음 더보기에서 다시 받음
                        error = message,
                        snackbarState = SnackbarState(message)
                    )
                }
            }
    }

    private fun loadMore() = intent {
        if (state.isLoading || !state.hasMore) return@intent
        if (state.sectionType == SectionType.PINNED_POST) return@intent  // 고정 포스트는 더 로드 없음

        reduce {
            state.copy(
                page = state.page + 1,
                isLoading = true
            )
        }
        loadData()
    }

    private fun refresh() = intent {
        listGeneration.next()  // 진행 중이던 더보기·첫 로드 버림
        reduce {
            state.copy(
                page = 1,
                isLoading = true,
                posts = emptyList(),
                products = emptyList(),
                hasMore = true,
                error = null
            )
        }
        loadData()
    }

    private fun navigateToPostDetail(postId: Int) = intent {
        postSideEffect(SectionDetailSideEffect.NavigateToPostDetail(postId))
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(SectionDetailSideEffect.NavigateToProductDetail(productId))
    }

    private fun showRestrictedSnackbar(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
