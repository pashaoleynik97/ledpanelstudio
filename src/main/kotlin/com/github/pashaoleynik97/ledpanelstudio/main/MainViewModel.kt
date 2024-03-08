package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.pashaoleynik97.ledpanelstudio.data.MRow
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
        val currentFrame: Int = 0,
        val currentSceneId: String? = null,
        val modulesCount: Int,
        val modulesDirection: Scopes.ProjectScope.Direction,
        val sceneToDelete: String? = null,
    ) {

        internal fun safeCurrentFrame(): Int {
            if (currentFrame > scenes.find { it.sceneId == currentSceneId }!!.frames.entries.indices.last) return 0
            return currentFrame
        }

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

        fun currentModules(): List<Module> {
            if (currentSceneId == null) return listOf()
            return scenes.find { it.sceneId == currentSceneId }!!.frames[safeCurrentFrame()]!!
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

    fun onLedClicked(moduleIndex: Int, row: Int, column: Int) {
        if (mState.value.currentSceneId == null) return
        val originalModule = mState.value.scenes.first {
            it.sceneId == mState.value.currentSceneId
        }.frames[mState.value.safeCurrentFrame()]!![moduleIndex]
        val newModule = modifyModule(originalModule, row, column, true) // todo change true on valid reference
        val newScenes = mState.value.scenes.map { scene ->
            if (scene.sceneId != mState.value.currentSceneId) scene else scene.copy(
                frames = hashMapOf(
                    *(scene.frames.entries.map { frame ->
                        if (frame.key != mState.value.safeCurrentFrame())
                            frame.toPair()
                        else
                            Pair(
                                frame.key,
                                frame.value.mapIndexed { mIndex, module ->
                                    if (mIndex != moduleIndex) module else newModule
                                }
                            )
                    }).toTypedArray()
                )
            )
        }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = newScenes
            )
        )
        mState.upd {
            copy(scenes = prjScope.scenes)
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

    private fun modifyModule(originalModule: Module, rowIndex: Int, ledIndex: Int, enabled: Boolean): Module {
        return originalModule.copy(
            r0 = if (rowIndex == 0) modifyRow(originalModule.r0, ledIndex, enabled) else originalModule.r0,
            r1 = if (rowIndex == 1) modifyRow(originalModule.r1, ledIndex, enabled) else originalModule.r1,
            r2 = if (rowIndex == 2) modifyRow(originalModule.r2, ledIndex, enabled) else originalModule.r2,
            r3 = if (rowIndex == 3) modifyRow(originalModule.r3, ledIndex, enabled) else originalModule.r3,
            r4 = if (rowIndex == 4) modifyRow(originalModule.r4, ledIndex, enabled) else originalModule.r4,
            r5 = if (rowIndex == 5) modifyRow(originalModule.r5, ledIndex, enabled) else originalModule.r5,
            r6 = if (rowIndex == 6) modifyRow(originalModule.r6, ledIndex, enabled) else originalModule.r6,
            r7 = if (rowIndex == 7) modifyRow(originalModule.r7, ledIndex, enabled) else originalModule.r7,
        )
    }

    private fun modifyRow(originalRow: MRow, ledIndex: Int, enabled: Boolean): MRow {
        return originalRow.copy(
            c0 = if (ledIndex == 0) enabled else originalRow.c0,
            c1 = if (ledIndex == 1) enabled else originalRow.c1,
            c2 = if (ledIndex == 2) enabled else originalRow.c2,
            c3 = if (ledIndex == 3) enabled else originalRow.c3,
            c4 = if (ledIndex == 4) enabled else originalRow.c4,
            c5 = if (ledIndex == 5) enabled else originalRow.c5,
            c6 = if (ledIndex == 6) enabled else originalRow.c6,
            c7 = if (ledIndex == 7) enabled else originalRow.c7,
        )
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

fun Module.toUiLedEnabledMatrix(): List<Pair<Int, Int>> {
    val rows = listOf(r0, r1, r2, r3, r4, r5, r6, r7)

    fun MRow.columns() = listOf(c0, c1, c2, c3, c4, c5, c6, c7)

    return rows.flatMapIndexed { rowIndex, row ->
        row.columns().mapIndexedNotNull { columnIndex, column ->
            if (column) Pair(rowIndex, columnIndex) else null
        }
    }
}