package com.ninezero.halo.core.fcm

object ActiveChatRoomTracker {
    @Volatile
    var activeRoomId: Int? = null
        private set

    fun enter(roomId: Int) {
        if (roomId > 0) activeRoomId = roomId
    }

    fun leave(roomId: Int) {
        // 다른 방으로 옮겼으면 덮지 않음
        if (activeRoomId == roomId) activeRoomId = null
    }
}
