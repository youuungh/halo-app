package com.ninezero.halo.feature.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.notification.domain.event.NotificationSyncBus
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.domain.model.NotificationDestination
import com.ninezero.halo.feature.notification.domain.model.NotificationEvent
import com.ninezero.halo.feature.notification.domain.model.resolveNotificationDestination
import com.ninezero.halo.feature.notification.domain.repository.NotificationEventRepository
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import com.ninezero.halo.feature.notification.fcm.TrayNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationSyncBus: NotificationSyncBus,
    private val trayNotifications: TrayNotifications
) : ViewModel(), OrbitContainerHost<NotificationState, NotificationState, NotificationSideEffect> {

    // init에서 즉시 로드하므로 초기 상태를 로딩으로 둠
    override val container: OrbitContainer<NotificationState, NotificationState, NotificationSideEffect> =
        orbitContainer(NotificationState(isLoading = true))

    private val listGeneration = ListGeneration()

    init {
        loadNotifications()
        observeWebSocket()
    }

    fun onIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.LoadNotifications -> loadNotifications()
            is NotificationIntent.RefreshNotifications -> refreshNotifications()
            is NotificationIntent.RefreshOnResume -> refreshNotificationsQuietly()
            is NotificationIntent.LoadMore -> loadMore()
            is NotificationIntent.DeleteNotification -> deleteNotification(intent.notificationId)
            is NotificationIntent.UndoDelete -> undoDelete()
            is NotificationIntent.SnackbarDismissed -> dismissSnackbar()
            is NotificationIntent.DismissToast -> dismissToast()
            is NotificationIntent.NavigateToTarget -> navigateToTarget(intent.notification)
            is NotificationIntent.NavigateBack -> intent { postSideEffect(NotificationSideEffect.NavigateBack) }
        }
    }

    private fun observeWebSocket() {
        notificationEventRepository.events
            .onEach { event ->
                when (event) {
                    is NotificationEvent.NewNotification -> {
                        intent {
                            reduce {
                                // 로딩과 WS 겹치면 중복 key
                                if (state.notifications.any { it.id == event.notification.id }) {
                                    state
                                } else {
                                    state.copy(
                                        notifications = listOf(event.notification) + state.notifications
                                    )
                                }
                            }
                        }
                    }

                    NotificationEvent.RefreshRequired -> refreshNotifications()
                    is NotificationEvent.Connected -> refreshNotificationsQuietly()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadNotifications() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        repository.getNotifications(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isLoading = false,
                        notifications = result.notifications.filter { it.id != state.deletedNotification?.id },
                        hasMore = result.nextCursor != null,
                        nextCursor = result.nextCursor
                    )
                }
                notificationSyncBus.notifyChanged()
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "알림을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshNotifications() = intent {
        reduce { state.copy(isRefreshing = true) }

        repository.getNotifications(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isRefreshing = false,
                        // 삭제 대기분은 서버에 아직 있어 새 목록에서 제외
                        notifications = result.notifications.filter { it.id != state.deletedNotification?.id },
                        hasMore = result.nextCursor != null,
                        nextCursor = result.nextCursor,
                        error = null
                    )
                }
                notificationSyncBus.notifyChanged()
            }
            .onFailure {
                reduce { state.copy(isRefreshing = false, toastMessage = "새로고침에 실패했습니다") }
            }
    }

    // 첫 페이지만 받아 앞에 합침
    private fun refreshNotificationsQuietly() = intent {
        repository.getNotifications(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    val fresh = result.notifications.filter { it.id != state.deletedNotification?.id }
                    val merged = state.notifications.mergeFreshHead(fresh) { it.id }
                    // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                    val keepsTail = merged.size > fresh.size
                    state.copy(
                        notifications = merged,
                        hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                        nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                        error = null
                    )
                }
                notificationSyncBus.notifyChanged()
            }
    }

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isLoading || state.isRefreshing || !state.hasMore) return@intent

        val generation = listGeneration.current()
        val cursor = state.nextCursor
        reduce { state.copy(isLoading = true) }

        repository.getNotifications(cursor = cursor, limit = 20)
            .onSuccess { result ->
                if (listGeneration.current() != generation) {
                    reduce { state.copy(isLoading = false) }
                    // 목록 수가 그대로라 트리거 대신 다시 요청
                    if (state.hasMore) loadMore()
                    return@intent
                }
                reduce {
                    state.copy(
                        isLoading = false,
                        notifications = (state.notifications + result.notifications.filter { it.id != state.deletedNotification?.id }).distinctBy { it.id },
                        hasMore = result.nextCursor != null,
                        nextCursor = result.nextCursor
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isLoading = false, toastMessage = "알림을 더 불러오지 못했습니다") }
            }
    }

    private fun markAsRead(notificationId: Int) = intent {
        val notification = state.notifications.find { it.id == notificationId } ?: return@intent
        if (notification.isRead) return@intent

        reduce {
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                }
            )
        }

        repository.markAsRead(notificationId)
            .onSuccess {
                // 인앱에서 읽었으면 트레이에서 제거
                trayNotifications.cancel(notification)
                notificationSyncBus.notifyChanged()
            }
            .onFailure {
                reduce {
                    state.copy(
                        notifications = state.notifications.map {
                            if (it.id == notificationId) it.copy(isRead = false) else it
                        }
                    )
                }
            }
    }

    private fun deleteNotification(notificationId: Int) = intent {
        val notification = state.notifications.find { it.id == notificationId } ?: return@intent
        val pending = state.deletedNotification  // 직전 삭제 대기분

        reduce {
            state.copy(
                notifications = state.notifications.filter { it.id != notificationId },
                deletedNotification = notification,
                snackbarState = SnackbarState(
                    message = "알림이 삭제되었습니다",
                    actionLabel = "취소"
                )
            )
        }

        // 교체 전 명시 커밋
        if (pending != null) {
            repository.deleteNotification(pending.id)
                .onSuccess {
                    // 대기 중 새로고침으로 되돌아온 항목 제거
                    reduce { state.copy(notifications = state.notifications.filter { it.id != pending.id }) }
                    trayNotifications.cancel(pending)
                    notificationSyncBus.notifyChanged()
                }
                .onFailure {
                    val restored = (state.notifications + pending).distinctBy { it.id }.sortedByDescending { it.createdAt }
                    reduce {
                        state.copy(
                            notifications = restored
                        )
                    }
                    notificationSyncBus.notifyChanged()
                }
        }
    }

    private fun undoDelete() = intent {
        val deleted = state.deletedNotification ?: return@intent
        val restored = (state.notifications + deleted).distinctBy { it.id }.sortedByDescending { it.createdAt }  // 원래 위치에 복원

        reduce {
            state.copy(
                notifications = restored,
                deletedNotification = null,
                snackbarState = null
            )
        }
    }

    private fun dismissSnackbar() = intent {
        val deleted = state.deletedNotification

        reduce { state.copy(snackbarState = null, deletedNotification = null) }

        if (deleted != null) {
            repository.deleteNotification(deleted.id)
                .onSuccess {
                    // 대기 중 새로고침으로 되돌아온 항목 제거
                    reduce { state.copy(notifications = state.notifications.filter { it.id != deleted.id }) }
                    trayNotifications.cancel(deleted)
                    notificationSyncBus.notifyChanged()
                }
                .onFailure {
                    val restored =
                        (state.notifications + deleted).distinctBy { it.id }.sortedByDescending { it.createdAt }
                    reduce {
                        state.copy(
                            notifications = restored,
                            snackbarState = SnackbarState(message = "삭제에 실패했습니다")
                        )
                    }
                    notificationSyncBus.notifyChanged()
                }
        }
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun navigateToTarget(notification: Notification) = intent {
        if (!notification.isRead) {
            markAsRead(notification.id)
        }

        when (
            val destination = resolveNotificationDestination(
                targetType = notification.targetType,
                targetId = notification.targetId,
                deepLink = notification.deepLink,
                notificationType = notification.type
            )
        ) {
            is NotificationDestination.Post ->
                postSideEffect(
                    NotificationSideEffect.NavigateToPost(
                        destination.postId,
                        destination.highlightCommentId
                    )
                )

            is NotificationDestination.Comment ->
                postSideEffect(
                    NotificationSideEffect.NavigateToComment(
                        destination.commentId,
                        destination.highlightReplyId
                    )
                )

            is NotificationDestination.User -> postSideEffect(
                NotificationSideEffect.NavigateToUser(
                    destination.userId
                )
            )

            is NotificationDestination.Order -> postSideEffect(
                NotificationSideEffect.NavigateToOrder(
                    destination.orderId
                )
            )

            is NotificationDestination.ChatRoom -> postSideEffect(
                NotificationSideEffect.NavigateToChatRoom(
                    destination.roomId
                )
            )

            is NotificationDestination.Product -> postSideEffect(
                NotificationSideEffect.NavigateToProduct(
                    destination.productId
                )
            )

            NotificationDestination.Point -> postSideEffect(NotificationSideEffect.NavigateToPoint)
            NotificationDestination.CreatorApplication -> {
                postSideEffect(NotificationSideEffect.NavigateToCreatorApplication)
            }

            NotificationDestination.MySubscriptions -> {
                postSideEffect(NotificationSideEffect.NavigateToMySubscriptions)
            }

            NotificationDestination.SalesManage -> postSideEffect(NotificationSideEffect.NavigateToSalesManage)
            is NotificationDestination.ProductReviews -> postSideEffect(
                NotificationSideEffect.NavigateToReviewList(
                    destination.productId,
                    destination.reviewId
                )
            )

            null -> Unit
        }
    }
}
