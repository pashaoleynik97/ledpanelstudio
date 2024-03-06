package com.github.pashaoleynik97.ledpanelstudio.main.data

interface SceneUiItem {
    val id: String
}

data class SceneItem(
    val name: String,
    val selected: Boolean,
    override val id: String
) : SceneUiItem

data class NoScenesItem(
    override val id: String = "_noScenesId"
): SceneUiItem