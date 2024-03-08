package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateOf
import com.github.pashaoleynik97.ledpanelstudio.data.MRow
import com.github.pashaoleynik97.ledpanelstudio.data.Module
import com.github.pashaoleynik97.ledpanelstudio.data.Scene
import com.github.pashaoleynik97.ledpanelstudio.main.data.*
import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes
import com.github.pashaoleynik97.ledpanelstudio.ui.LSSection
import com.github.pashaoleynik97.ledpanelstudio.utils.insertAt
import com.github.pashaoleynik97.ledpanelstudio.utils.swap
import com.github.pashaoleynik97.ledpanelstudio.utils.upd
import kotlinx.coroutines.*

class MainViewModel {

    private val animScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var animJob: Job? = null

    interface UiCallbacks {
        fun showSceneDeleteDialog(sceneName: String)
        fun showNewProjectDialog()
    }

    var uiCallbacks: UiCallbacks? = null

    //==============================================================================================
    // *** State ***
    //==============================================================================================

    // region [State]

    enum class Presentation {
        EDITOR, PREVIEW
    }

    enum class Tool {
        PEN, ERASER, SMART
    }

    data class VmState(
        val presentation: Presentation = Presentation.EDITOR,
        val scenes: List<Scene>,
        val currentFrame: Int = 0,
        val currentSceneId: String? = null,
        val modulesCount: Int,
        val modulesDirection: Scopes.ProjectScope.Direction,
        val sceneToDelete: String? = null,
        val tool: Tool = Tool.SMART,
        val playing: Boolean = false
    ) {

        internal fun safeCurrentFrame(): Int {
            if (currentFrame > scenes.find { it.sceneId == currentSceneId }!!.frames.indices.last) return 0
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

        fun framesList(): List<FrameItem> {
            if (scenes.isEmpty()) return listOf()
            val scene = scenes.first {
                it.sceneId == currentSceneId
            }
            val frames = scene.frames
            val framesTime = scene.framesTime
            return frames.mapIndexed { fIndex, frame ->
                FrameItem(
                    number = fIndex,
                    selected = fIndex == safeCurrentFrame(),
                    fwdAvailable = fIndex != frames.indices.last,
                    bwdAvailable = fIndex != 0,
                    duration = framesTime[fIndex],
                    modules = frame
                )
            }
        }

        fun sceneProperties(): ScenePropertiesUiData? {
            if (currentSceneId == null) return null
            return ScenePropertiesUiData(
                iterations = scenes.find { it.sceneId == currentSceneId }!!.iterations,
                interstitial = false, // todo change to actual
                frames = scenes.find { it.sceneId == currentSceneId }!!.frames.size
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
            return scenes.find { it.sceneId == currentSceneId }!!.frames[safeCurrentFrame()]
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
        if (p == Presentation.EDITOR) {
            animJob?.cancel()
            mState.upd {
                copy(
                    playing = false
                )
            }
        }
        mState.upd {
            copy(presentation = p)
        }
    }

    fun onModulesDirectionChanged(direction: Scopes.ProjectScope.Direction) {
        if (isAnimPlaying) return
        setupModulesDirection(direction)
    }

    fun onAddScenePressed() {
        if (isAnimPlaying) return
        addScene()
    }

    fun onDeleteScenePressed(sceneId: String) {
        if (isAnimPlaying) return
        mState.upd {
            copy(sceneToDelete = sceneId)
        }
        uiCallbacks?.showSceneDeleteDialog(mState.value.scenes.find { it.sceneId == sceneId }!!.name)
    }

    fun onDeleteSceneConfirmed() {
        if (isAnimPlaying) return
        deleteScene()
    }

    fun onDeleteSceneRejected() {
        if (isAnimPlaying) return
        mState.upd {
            copy(sceneToDelete = null)
        }
    }

    fun onSceneIterationsIncrement() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val cntIterations = mState.value.scenes.find { it.sceneId == mState.value.currentSceneId }!!.iterations
        if (cntIterations >= 500) return
        updateSceneIterations(cntIterations + 1)
    }

    fun onSceneIterationsDecrement() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val cntIterations = mState.value.scenes.find { it.sceneId == mState.value.currentSceneId }!!.iterations
        if (cntIterations < 2) return
        updateSceneIterations(cntIterations - 1)
    }

    fun onSceneClicked(sceneId: String) {
        if (isAnimPlaying) return
        mState.upd {
            copy(currentSceneId = sceneId)
        }
    }

    fun onLedClicked(moduleIndex: Int, row: Int, column: Int) {
        if (isAnimPlaying) return
        if (mState.value.presentation == Presentation.PREVIEW) return
        if (mState.value.currentSceneId == null) return
        val originalModule = mState.value.scenes.first {
            it.sceneId == mState.value.currentSceneId
        }.frames[mState.value.safeCurrentFrame()][moduleIndex]
        val newModule = modifyModule(
            originalModule = originalModule,
            rowIndex = row,
            ledIndex = column,
            enabled = when (mState.value.tool) {
                Tool.PEN -> true
                Tool.ERASER -> false
                Tool.SMART -> originalModule.row(row).led(column).not()
            }
        )
        val newScenes = mState.value.scenes.map { scene ->
            if (scene.sceneId != mState.value.currentSceneId) scene else scene.copy(
                frames = scene.frames.mapIndexed { fIndex, frame ->
                        if (fIndex != mState.value.safeCurrentFrame())
                            frame
                        else
                            frame.mapIndexed { mIndex, module ->
                                if (mIndex != moduleIndex) module else newModule
                            }
                    }
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

    fun onToolSelected(tool: Tool) {
        if (isAnimPlaying) return
        mState.upd {
            copy(tool = tool)
        }
    }

    fun onNewProjectClicked() {
        if (isAnimPlaying) return
        uiCallbacks?.showNewProjectDialog()
    }

    fun onNewProjectCreationAccepted(modules: Int) {
        if (isAnimPlaying) return
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            Scopes.ProjectScope(
                modulesCount = modules
            )
        )
        mState.upd {
            VmState(
                scenes = prjScope.scenes,
                modulesCount = prjScope.modulesCount,
                modulesDirection = prjScope.direction
            )
        }
        addScene()
    }

    fun onFrameClicked(number: Int) {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        mState.upd {
            copy(currentFrame = number)
        }
    }

    fun onAddFrameClicked() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val oldScene = prjScope.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!
        val oldFrames = oldScene.frames
        val newKey = oldFrames.indices.last + 1
        val newFrames = oldFrames.insertAt(
            newKey,
            mutableListOf<Module>().also { list ->
                repeat(prjScope.modulesCount) { i -> list.add(Module(ordinal = i)) }
            }
        )
        val oldTime = oldScene.framesTime
        val newTime = oldTime.insertAt(newKey, oldTime.last())
        val newScene = oldScene.copy(
            frames = newFrames,
            framesTime = newTime
        )
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = prjScope.scenes.map {
                    if (it.sceneId == oldScene.sceneId) newScene else it
                }
            )
        )
        mState.upd {
            copy(
                scenes = prjScope.scenes
            )
        }
    }

    fun onDeleteFrameClicked() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val deleteKey = mState.value.safeCurrentFrame()
        val oldScene = prjScope.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!
        if (oldScene.frames.size < 2) return
        val newFrames = oldScene.frames.toMutableList().apply { removeAt(deleteKey) }
        val newFrameTimes = oldScene.framesTime.toMutableList().apply { removeAt(deleteKey) }
        val newScene = oldScene.copy(
            frames = newFrames,
            framesTime = newFrameTimes
        )
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = prjScope.scenes.map {
                    if (it.sceneId == oldScene.sceneId) newScene else it
                }
            )
        )
        val suggestedNewKey = deleteKey - 1
        val newSelection = if (deleteKey == 0) {
            0
        } else if (suggestedNewKey > newScene.frames.indices.last) {
            newScene.frames.indices.last
        } else {
            suggestedNewKey
        }
        mState.upd {
            copy(
                scenes = prjScope.scenes,
                currentFrame = newSelection
            )
        }
    }

    fun onCopyFrameClicked() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val copyKey = mState.value.safeCurrentFrame()
        val oldScene = prjScope.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!
        val oldSceneIndex = prjScope.scenes.indexOfFirst {
            it.sceneId == mState.value.currentSceneId
        }
        val oldTime = oldScene.framesTime[copyKey]
        val frameCopy = oldScene.frames[copyKey].toList()
        val newFrames = oldScene.frames.insertAt(copyKey + 1, frameCopy)
        val newFrameTimes = oldScene.framesTime.insertAt(copyKey + 1, oldTime)
        val newScene = oldScene.copy(
            frames = newFrames,
            framesTime = newFrameTimes
        )
        val newScenes = prjScope.scenes.toMutableList().apply { this[oldSceneIndex] = newScene }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = newScenes
            )
        )
        val newSelection = copyKey + 1
        mState.upd {
            copy(
                scenes = prjScope.scenes,
                currentFrame = newSelection
            )
        }
    }

    fun onFrameTimeSelected(frameNumber: Int, frameTime: Long) {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        val oldScene = prjScope.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!
        val oldSceneIndex = prjScope.scenes.indexOfFirst {
            it.sceneId == mState.value.currentSceneId
        }
        val newTimes = oldScene.framesTime.toMutableList().apply { this[frameNumber] = frameTime }
        val newScene = oldScene.copy(
            framesTime = newTimes
        )
        val newScenes = prjScope.scenes.toMutableList().apply { this[oldSceneIndex] = newScene }
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = newScenes
            )
        )
        mState.upd {
            copy(
                scenes = prjScope.scenes
            )
        }
    }

    fun onFrameMoveBwdClicked(frameNumber: Int) {
        if (isAnimPlaying) return
        swapFrames(frameNumber, frameNumber - 1)
    }

    fun onFrameMoveFwdClicked(frameNumber: Int) {
        if (isAnimPlaying) return
        swapFrames(frameNumber, frameNumber + 1)
    }

    fun onPlayClicked() {
        if (isAnimPlaying) return
        if (mState.value.currentSceneId == null) return
        mState.upd {
            copy(
                presentation = Presentation.PREVIEW,
                playing = true
            )
        }
        animJob?.cancel()
        animJob = null
        val animFrames = mState.value.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!.frames.indices
        val animTimings = mState.value.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!.framesTime
        animJob = animScope.launch {
            while (true) {
                animFrames.forEach { index ->
                    mState.upd {
                        copy(currentFrame = index)
                    }
                    delay(animTimings[index])
                }
            }
        }
    }

    fun onStopClicked() {
        if (mState.value.currentSceneId == null) return
        animJob?.cancel()
        animJob = null
        mState.upd {
            copy(
                playing = false
            )
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
                    frames = listOf(
                        mutableListOf<Module>().also { list ->
                            repeat(prjScope.modulesCount) { i -> list.add(Module(ordinal = i)) }
                        }
                    ),
                    framesTime = listOf(
                        500L
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

    private fun swapFrames(i1: Int, i2: Int) {
        if (mState.value.currentSceneId == null) return
        val oldScene = prjScope.scenes.find {
            it.sceneId == mState.value.currentSceneId
        }!!
        val oldSceneIndex = prjScope.scenes.indexOfFirst {
            it.sceneId == mState.value.currentSceneId
        }
        val newFrames = oldScene.frames.swap(i1, i2)
        val newTimes = oldScene.framesTime.swap(i1, i2)
        val newScene = oldScene.copy(
            frames = newFrames,
            framesTime = newTimes
        )
        Scopes.updateScope(
            Scopes.ScopeKey.Project,
            prjScope.copy(
                scenes = prjScope.scenes.toMutableList().apply { this[oldSceneIndex] = newScene }
            )
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

    private fun Module.row(i: Int) = when (i) {
        0 -> r0
        1 -> r1
        2 -> r2
        3 -> r3
        4 -> r4
        5 -> r5
        6 -> r6
        7 -> r7
        else -> throw IllegalArgumentException("Module does not contain row with index $i")
    }

    private fun MRow.led(i: Int) = when (i) {
        0 -> c0
        1 -> c1
        2 -> c2
        3 -> c3
        4 -> c4
        5 -> c5
        6 -> c6
        7 -> c7
        else -> throw IllegalArgumentException("MRow does not contain LED with index $i")
    }

    // endregion

    //==============================================================================================
    // *** Utils ***
    //==============================================================================================

    // region [Utils]

    private val prjScope: Scopes.ProjectScope
        get() = Scopes.getOrOpenScope(Scopes.ScopeKey.Project)

    private val isAnimPlaying: Boolean
        get() = mState.value.playing

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