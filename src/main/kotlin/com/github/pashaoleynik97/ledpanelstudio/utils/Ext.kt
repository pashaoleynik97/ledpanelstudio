package com.github.pashaoleynik97.ledpanelstudio.utils

import androidx.compose.runtime.MutableState

inline fun <reified T> MutableState<T>.upd(block: (T.() -> T)) {
    val oldState = this.value
    this.value = block.invoke(oldState)
}

fun <K, V> HashMap<K, V>.deleteByKeyAndShift(keyToRemove: K): HashMap<K, V> {
    val resultMap = HashMap<K, V>()
    var shift = false
    var lastKey: K? = null
    for ((key, value) in this) {
        if (key == keyToRemove) {
            shift = true
            lastKey = key
            continue
        }
        if (shift) {
            resultMap[lastKey!!] = value
            lastKey = key
        } else {
            resultMap[key] = value
        }
    }
    return resultMap
}

fun <V> List<V>.insertAt(index: Int, v: V): List<V> {
    if (isEmpty() && index != 0) throw IndexOutOfBoundsException("insertAt($index,v) on empty list")
    if (isEmpty() && index == 0) return listOf(v)
    if (index in indices) return subList(0, index) + v + subList(index, indices.last + 1)
    return this + v
}
