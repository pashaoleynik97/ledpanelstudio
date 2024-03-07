package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.pashaoleynik97.ledpanelstudio.data.Module
import com.github.pashaoleynik97.ledpanelstudio.data.Scene
import com.github.pashaoleynik97.ledpanelstudio.main.data.*
import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes
import com.github.pashaoleynik97.ledpanelstudio.ui.LSSection
import com.github.pashaoleynik97.ledpanelstudio.utils.upd

class MainViewModel {

    interface UiCallbacks {
        fun showSceneDeleteDialog(sceneName: String)
    }

    var uiCallbacks: UiCallbacks? = null

    //==============================================================================================
    // *** State ***
    //==============================================================================================

    // region [State]

    enum class Presentation {
        EDITOR, PREVIEW
    }

    data class VmState(
        val presentation: Presentation = Presentation.EDITOR,
        val scenes: List<Scene>,
        val currentSceneId: String? = null,
        val modulesCount: Int,
        val modulesDirection: Scopes.ProjectScope.Direction,
        val sceneToDelete: String? = null
    ) {

        fun presentationSections(): List<LSSection> {
            return listOf(
                LSSection(
                    caption = "Editor",
                    selected = presentation == Presentation.EDITOR,
                    tag = Presentation.EDITOR
                ),
                LSSection(
                    caption = "Preview",
                    selected = presentation == Presentation.PREVIEW,
                    tag = Presentation.PREVIEW
                ),
            )
        }

        fun scenesList(): List<SceneUiItem> {
            return if (scenes.isNotEmpty()) {
                scenes.map {
                    SceneItem(
                        name = it.name,
                        selected = it.sceneId == currentSceneId,
                        id = it.sceneId
                    )
                }
            } else listOf(
                NoScenesItem()
            )
        }

        fun sceneProperties(): ScenePropertiesUiData? {
            if (currentSceneId == null) return null
            return ScenePropertiesUiData(
                iterations = scenes.find { it.sceneId == currentSceneId }!!.iterations,
                interstitial = false // todo change to actual
            )
        }

        fun projectProperties(): ProjectPropertiesUiData {
            return ProjectPropertiesUiData(
                modules = modulesCount,
                direction = modulesDirection
            )
        }

    }

    private val mState: MutableState<VmState> = mutableStateOf(
        VmState(
            scenes = prjScope.scenes,
            modulesCount = prjScope.modulesCount,
            modulesDirection = prjScope.direction
        )
    )
    val state: MutableState<VmState> get() = mState

    // endregion

    //==============================================================================================
    // *** API ***
    //==============================================================================================

    // region [API]

    fun switchPresentation(p: Presentation) {
        mState.upd {
            copy(presentation = p)
        }
    }

    fun onModulesDirectionChanged(direction: Scopes.ProjectScope.Direction) {
        setupModulesDirection(direction)
    }

    fun onAddScenePressed() {
        addScene()
    }

    fun onDeleteScenePressed(sceneId: String) {
        mState.upd {
            copy(sceneToDelete = sceneId)
        }
        uiCallbacks?.showSceneDeleteDialog(mState.value.scenes.find { it.sceneId == sceneId }!!.name)
    }

    fun onDeleteSceneConfirmed() {
        deleteScene()
    }

    fun onDeleteSceneRejected() {
        mState.upd {
            copy(sceneToDelete = null)
        }
    }

    fun onSceneIterationsIncrement() {
        if (mState.value.currentSceneId == null) return
        val cntIterations = mState.value.scenes.find { it.sceneId == mState.value.currentSceneId }!!.iterations
        if (cntIterations >= 500) return
        updateSceneIterations(cntIterations + 1)
    }

    fun onSceneIterationsDecrement() {
        if (mState.value.currentSceneId == null) return
        val cntIterations = mState.value.scenes.find { it.sceneId == mState.value.currentSceneId }!!.iterations
        if (cntIterations < 2) return
        updateSceneIterations(cntIterations - 1)
    }

    fun onSceneClicked(sceneId: String) {
        mState.upd {
            copy(currentSceneId = sceneId)
        }
    }

    // endregion

    //==============================================================================================
    // *** Domain ***
    //==============================================================================================

    // region [Domain]

    private fun setupModulesDirection(direction: Scopes.ProjectScope.Direction) {
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(direction = direction)
        )
        mState.upd {
            copy(
                modulesDirection = prjScope.direction
            )
        }
    }

    private fun addScene() {
        val newSceneId: String
        val newScenesList = prjScope.scenes.toMutableList().apply {
            add(
                Scene(
                    frames = hashMapOf(
                        Pair(0, mutableListOf<Module>().also { list ->
                            repeat(prjScope.modulesCount) { i -> list.add(Module(ordinal = i)) }
                        }.toList())
                    ),
                    name = "Scene_${System.currentTimeMillis().hashCode()}"
                ).also { newSceneId = it.sceneId }
            )
        }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(scenes = newScenesList)
        )
        mState.upd {
            copy(
                scenes = prjScope.scenes,
                currentSceneId = newSceneId
            )
        }
    }

    private fun deleteScene() {
        val newScenesList = prjScope.scenes.toMutableList().apply {
            removeIf { it.sceneId == mState.value.sceneToDelete }
        }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(scenes = newScenesList)
        )
        if (mState.value.currentSceneId == mState.value.sceneToDelete) {
            mState.upd {
                copy(
                    currentSceneId = null
                )
            }
        }
        mState.upd {
            copy(
                scenes = prjScope.scenes,
                sceneToDelete = null
            )
        }
    }

    private fun updateSceneIterations(newIterations: Int) {
        val newScenesList = prjScope.scenes.map {
            if (it.sceneId != mState.value.currentSceneId) it else it.copy(iterations = newIterations)
        }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(scenes = newScenesList)
        )
        mState.upd {
            copy(
                scenes = prjScope.scenes
            )
        }
    }

    // endregion

    //==============================================================================================
    // *** Utils ***
    //==============================================================================================

    // region [Utils]

    private val prjScope: Scopes.ProjectScope
        get() = Scopes.getOrOpenScope(Scopes.ScopeKey.Project)

    // endregion

}