package com.si.myfoodordering.ui.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Messages courts pour Toast affichés depuis la racine de navigation (une seule collecte). */
object UiMessageBus {
    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun toast(text: String) {
        _messages.tryEmit(text)
    }
}
