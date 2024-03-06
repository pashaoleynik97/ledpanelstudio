package com.github.pashaoleynik97.ledpanelstudio.main.data

import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes

data class ScenePropertiesUiData(
    val iterations: Int,
    val interstitial: Boolean
)

data class ProjectPropertiesUiData(
    val modules: Int,
    val direction: Scopes.ProjectScope.Direction
)