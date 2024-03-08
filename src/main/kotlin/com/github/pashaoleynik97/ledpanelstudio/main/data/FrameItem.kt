package com.github.pashaoleynik97.ledpanelstudio.main.data

import com.github.pashaoleynik97.ledpanelstudio.data.Module

data class FrameItem(
    val number: Int,
    val selected: Boolean,
    val fwdAvailable: Boolean,
    val bwdAvailable: Boolean,
    val duration: Long,
    val modules: List<Module>
) {
    val displayNumber get() = number + 1
}