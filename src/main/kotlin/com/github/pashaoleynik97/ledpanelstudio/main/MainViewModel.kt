package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.pashaoleynik97.ledpanelstudio.ui.LSSection
import com.github.pashaoleynik97.ledpanelstudio.utils.upd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.xml.stream.events.EndDocument

class MainViewModel {

    enum class Presentation {
        EDITOR, PREVIEW
    }

    data class VmState(
        val presentation: Presentation = Presentation.EDITOR
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

    }

    private val mState: MutableState<VmState> = mutableStateOf(VmState())
    val state: MutableState<VmState> get() = mState

    fun switchPresentation() {
        mState.upd {
            copy(presentation = if (mState.value.presentation == Presentation.EDITOR) Presentation.PREVIEW else Presentation.EDITOR)
        }
    }

}