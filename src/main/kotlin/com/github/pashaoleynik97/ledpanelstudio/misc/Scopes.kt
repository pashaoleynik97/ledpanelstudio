package com.github.pashaoleynik97.ledpanelstudio.misc

import com.github.pashaoleynik97.ledpanelstudio.data.Scene

object Scopes {

    enum class ScopeKey {
        Project
    }

    //==============================================================================================
    // *** Scopes ***
    //==============================================================================================

    // region [Scopes]

    private var mProjectScope: ProjectScope? = null

    // endregion

    //==============================================================================================
    // *** Utils ***
    //==============================================================================================

    // region [Utils]

    @Suppress("UNCHECKED_CAST")
    fun <T : Scope> getOrOpenScope(key: ScopeKey): T = when (key) {
        ScopeKey.Project -> {
            if (mProjectScope == null) mProjectScope = ProjectScope()
            mProjectScope!! as T
        }
    }

    fun updateScope(key: ScopeKey, newScope: Scope) {
        when (key) {
            ScopeKey.Project -> {
                getOrOpenScope<ProjectScope>(ScopeKey.Project)
                mProjectScope = newScope as ProjectScope
            }
        }
    }

    fun closeScope(key: ScopeKey) = when (key) {
        ScopeKey.Project -> mProjectScope = null
    }

    // endregion

    //==============================================================================================
    // *** Scope states ***
    //==============================================================================================

    // region [Scope states]

    interface Scope

    data class ProjectScope(
        val modulesCount: Int = 1,
        val direction: Direction = Direction.RTL,
        val scenes: List<Scene> = listOf(),
        val interstitialSceneId: String? = null,
    ) : Scope {

        enum class Direction {
            LTR, RTL
        }

    }

    // endregion

}