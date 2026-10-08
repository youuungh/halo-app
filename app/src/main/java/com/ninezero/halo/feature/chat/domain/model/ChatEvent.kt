package com.ninezero.halo.feature.chat.domain.model

sealed class ChatEvent {
    data object Connected : ChatEvent()
    data class MessageReceived(val message: Message) : ChatEvent()
    data class MessagesRead(val roomId: Int, val messageIds: List<Int>, val readerId: Int) : ChatEvent()
    data class MessageDeleted(val roomId: Int, val messageId: Int) : ChatEvent()
    data class Error(val error: String) : ChatEvent()
}
