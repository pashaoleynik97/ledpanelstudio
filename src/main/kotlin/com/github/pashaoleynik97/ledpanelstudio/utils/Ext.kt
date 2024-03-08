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

fun <V> List<V>.swap(i1: Int, i2: Int): List<V> {
    if (i1 !in indices || i2 !in indices) throw IndexOutOfBoundsException("Given indices $i1, $i2 not in List bounds")
    val copy = this.toMutableList()
    return copy.also {
        it[i2] = this[i1]
        it[i1] = this[i2]
    }
}
