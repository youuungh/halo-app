package com.ninezero.halo.feature.chat.domain.repository

import com.ninezero.halo.feature.chat.domain.model.ChatEvent
import kotlinx.coroutines.flow.Flow

interface ChatEventRepository {
    val events: Flow<ChatEvent>
}
