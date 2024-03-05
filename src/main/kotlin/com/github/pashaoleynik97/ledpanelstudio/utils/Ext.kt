package com.github.pashaoleynik97.ledpanelstudio.utils

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow

inline fun <reified T> MutableStateFlow<T>.upd(block: (T.() -> T)) {
    val oldState = this.value
    this.value = block.invoke(oldState)
}

inline fun <reified T> MutableState<T>.upd(block: (T.() -> T)) {
    val oldState = this.value
    this.value = block.invoke(oldState)
}