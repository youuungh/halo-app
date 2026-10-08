package com.ninezero.halo.core.network.websocket

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RealtimeFrame(
    val type: String,
    val data: JsonElement? = null,
    val message: String? = null
)

object RealtimeFrameType {
    const val MESSAGE = "MESSAGE"
    const val MESSAGE_READ = "MESSAGE_READ"
    const val MESSAGE_DELETED = "MESSAGE_DELETED"
    const val NOTIFICATION = "NOTIFICATION"
    const val CONNECTED = "CONNECTED"
    const val ERROR = "ERROR"
}
