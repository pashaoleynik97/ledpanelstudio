package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.pashaoleynik97.ledpanelstudio.data.Module
import com.github.pashaoleynik97.ledpanelstudio.main.data.*
import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes
import com.github.pashaoleynik97.ledpanelstudio.ui.*
import java.awt.Dimension

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {

    val vmState = viewModel.state.value
    println(vmState)

    var showDeleteDialog by remember { mutableStateOf(Pair(false, "")) }
    var showNewProjectDialog by remember { mutableStateOf(false) }

    val uiCallbacks = remember {
        object : MainViewModel.UiCallbacks {
            override fun showSceneDeleteDialog(sceneName: String) {
                showDeleteDialog = Pair(true, sceneName)
            }

            override fun showNewProjectDialog() {
                showNewProjectDialog = true
            }
        }
    }
    viewModel.uiCallbacks = uiCallbacks

    MaterialTheme(colors = darkColors()) {

        Column {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Cl.darkGrey)
            ) {

                LargeSwitch(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            vertical = 8.dp
                        ).align(Alignment.Center),
                    sections = vmState.presentationSections(),
                    onSectionClicked = {
                        viewModel.switchPresentation(it as MainViewModel.Presentation)
                    }
                )

            }

            Row {

                ScenesPane(
                    modifier = Modifier
                        .fillMaxHeight(),
                    scenesList = vmState.scenesList(),
                    sceneProperties = vmState.sceneProperties(),
                    projectProperties = vmState.projectProperties(),
                    onModulesDirectionSwitched = {
                        viewModel.onModulesDirectionChanged(it)
                    },
                    onAddScenePressed = {
                        viewModel.onAddScenePressed()
                    },
                    onDeleteScenePressed = {
                        viewModel.onDeleteScenePressed(it)
                    },
                    onSceneIterationsDecrement = {
                        viewModel.onSceneIterationsDecrement()
                    },
                    onSceneIterationsIncrement = {
                        viewModel.onSceneIterationsIncrement()
                    },
                    onSceneClicked = {
                        viewModel.onSceneClicked(it)
                    }
                )

                MainPane(
                    modifier = Modifier
                        .weight(1f),
                    modules = vmState.currentModules(),
                    frames = vmState.framesList(),
                    onLedClicked = { moduleIndex, row, column ->
                        viewModel.onLedClicked(
                            moduleIndex = moduleIndex,
                            row = row,
                            column = column
                        )
                    },
                    onAddFrameClicked = {
                        viewModel.onAddFrameClicked()
                    },
                    onDeleteFrameClicked = {
                        viewModel.onDeleteFrameClicked()
                    },
                    onFrameClicked = {
                        viewModel.onFrameClicked(it)
                    },
                    onFrameCopyClicked = {
                        viewModel.onCopyFrameClicked()
                    },
                    onFrameTimeSelected = { frameNumber, frameTime ->
                        viewModel.onFrameTimeSelected(frameNumber, frameTime)
                    },
                    onFrameMoveFwdClicked = {
                        viewModel.onFrameMoveFwdClicked(it)
                    },
                    onFrameMoveBwdClicked = {
                        viewModel.onFrameMoveBwdClicked(it)
                    },
                    onPlayClicked = {
                        viewModel.onPlayClicked()
                    },
                    onStopClicked = {
                        viewModel.onStopClicked()
                    }
                )

                ToolsPane(
                    currentTool = vmState.tool,
                    onToolClicked = {
                        viewModel.onToolSelected(it)
                    }
                )

            }

        }

        // Dialogs
        if (showDeleteDialog.first) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.onDeleteSceneRejected()
                    showDeleteDialog = Pair(false, "")
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete ${showDeleteDialog.second}?",
                        color = Color.White
                    )
                },
                buttons = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.onDeleteSceneConfirmed()
                                showDeleteDialog = Pair(false, "")
                            }
                        ) {
                            Text("Yes, delete")
                        }

                        Spacer(Modifier.size(8.dp))

                        Button(
                            onClick = {
                                viewModel.onDeleteSceneRejected()
                                showDeleteDialog = Pair(false, "")
                            }
                        ) {
                            Text("No, cancel")
                        }
                    }
                }
            )
        }

        if (showNewProjectDialog) {

            var modulesCount by remember { mutableStateOf(4) }

            AlertDialog(
                title = {
                    Text(
                        "New Project",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                onDismissRequest = {
                    showNewProjectDialog = false
                },
                text = {
                    Column {

                        Text(
                            "Choose the quantity of LED modules (this setting cannot be altered later).",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.size(48.dp))

                        Row(
                            Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally)
                        ) {

                            Text(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.CenterVertically),
                                text = "Modules:",
                                color = Color.White,
                                fontSize = 14.sp
                            )

                            Spacer(Modifier.size(16.dp))

                            NumberPickerUi(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.CenterVertically),
                                value = modulesCount,
                                onIncrement = {
                                    if (modulesCount <= 7) modulesCount += 1
                                },
                                onDecrement = {
                                    if (modulesCount >= 2) modulesCount -= 1
                                }
                            )

                        }

                        Spacer(Modifier.size(48.dp))

                        Text(
                            modifier = Modifier
                                .widthIn(300.dp, 600.dp),
                            text = "Please be aware that selecting 'Create' will generate a new project, and any unsaved " +
                                    "modifications in the current project will not be retained. Ensure that you have saved " +
                                    "all necessary changes to the current project before proceeding.",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                },
                buttons = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                showNewProjectDialog = false
                                viewModel.onNewProjectCreationAccepted(
                                    modules = modulesCount
                                )
                            }
                        ) {
                            Text("Create")
                        }

                        Spacer(Modifier.size(8.dp))

                        Button(
                            onClick = {
                                showNewProjectDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }

    }

}

@Composable
private fun ScenesPane(
    modifier: Modifier = Modifier,
    scenesList: List<SceneUiItem>,
    sceneProperties: ScenePropertiesUiData?,
    projectProperties: ProjectPropertiesUiData,
    onModulesDirectionSwitched: (Scopes.ProjectScope.Direction) -> Unit,
    onAddScenePressed: () -> Unit,
    onDeleteScenePressed: (sceneId: String) -> Unit,
    onSceneIterationsIncrement: () -> Unit,
    onSceneIterationsDecrement: () -> Unit,
    onSceneClicked: (sceneId: String) -> Unit
) {
    Box(
        modifier = modifier
            .width(250.dp)
            .background(color = Cl.lightGrey)
    ) {

        Column(
            Modifier
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    text = "Scenes",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )

                IconButton(
                    onClick = onAddScenePressed,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically),
                    content = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )

                Spacer(Modifier.width(8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                scenesList.forEach { item ->

                    item(key = item.id) {
                        when (item) {
                            is SceneItem -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            color = if (item.selected) Cl.selection else Cl.darkGrey,
                                            shape = RoundedCornerShape(4.dp)
                                        ).clickable {
                                            onSceneClicked(item.id)
                                        }
                                ) {

                                    Text(
                                        modifier = Modifier.padding(
                                            top = 12.dp,
                                            bottom = 12.dp,
                                            start = 16.dp
                                        ).weight(1f),
                                        text = item.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    IconButton(
                                        onClick = {
                                            onDeleteScenePressed.invoke(item.id)
                                        },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.CenterVertically),
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    )

                                    Spacer(Modifier.width(8.dp))

                                }
                                Spacer(Modifier.height(4.dp))
                            }

                            is NoScenesItem -> {
                                Text(
                                    modifier = Modifier.padding(
                                        top = 12.dp,
                                        bottom = 12.dp,
                                        start = 16.dp
                                    ).fillMaxWidth(),
                                    text = "No Scenes...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                }
            }

            if (sceneProperties != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = "Scene Properties",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp),
                            text = "Iterations:",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        NumberPickerUi(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterVertically),
                            value = sceneProperties.iterations,
                            onIncrement = onSceneIterationsIncrement,
                            onDecrement = onSceneIterationsDecrement
                        )

                        Spacer(Modifier.size(8.dp))
                    }

                    Spacer(Modifier.size(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp),
                            text = "Frames:",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Text(
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.CenterVertically)
                                .padding(end = 16.dp),
                            text = sceneProperties.frames.toString(),
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.size(8.dp))
                    }

                    Spacer(Modifier.size(8.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.5f)
                )

                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    text = "Project Properties",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp),
                        text = "Modules:",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentHeight()
                            .align(Alignment.CenterVertically)
                            .padding(end = 16.dp),
                        text = projectProperties.modules.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.size(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp),
                        text = "Direction:",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    NormalSwitch(
                        modifier = Modifier
                            .wrapContentSize(),
                        sections = listOf(
                            LSSection(
                                caption = "LTR",
                                selected = projectProperties.direction == Scopes.ProjectScope.Direction.LTR,
                                Scopes.ProjectScope.Direction.LTR
                            ),
                            LSSection(
                                caption = "RTL",
                                selected = projectProperties.direction == Scopes.ProjectScope.Direction.RTL,
                                Scopes.ProjectScope.Direction.RTL
                            )
                        )
                    ) {
                        onModulesDirectionSwitched(it as Scopes.ProjectScope.Direction)
                    }

                    Spacer(Modifier.size(8.dp))
                }

                Spacer(Modifier.height(16.dp))

            }

        }

    }
}

@Composable
private fun ToolsPane(
    modifier: Modifier = Modifier,
    currentTool: MainViewModel.Tool,
    onToolClicked: (MainViewModel.Tool) -> Unit
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .fillMaxHeight()
            .background(color = Cl.lightGrey)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            ToolButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                resourcePath = "svg/smart.svg",
                enabled = currentTool == MainViewModel.Tool.SMART,
                onClick = {
                    onToolClicked.invoke(MainViewModel.Tool.SMART)
                }
            )

            ToolButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                resourcePath = "svg/pen.svg",
                enabled = currentTool == MainViewModel.Tool.PEN,
                onClick = {
                    onToolClicked.invoke(MainViewModel.Tool.PEN)
                }
            )

            ToolButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                resourcePath = "svg/eraser.svg",
                enabled = currentTool == MainViewModel.Tool.ERASER,
                onClick = {
                    onToolClicked.invoke(MainViewModel.Tool.ERASER)
                }
            )

        }

    }

}

@Composable
private fun MainPane(
    modifier: Modifier = Modifier,
    modules: List<Module>,
    frames: List<FrameItem>,
    onLedClicked: (moduleIndex: Int, row: Int, column: Int) -> Unit,
    onAddFrameClicked: () -> Unit,
    onDeleteFrameClicked: () -> Unit,
    onFrameClicked: (number: Int) -> Unit,
    onFrameCopyClicked: () -> Unit,
    onFrameTimeSelected: (frameNumber: Int, frameTime: Long) -> Unit,
    onFrameMoveBwdClicked: (frameNumber: Int) -> Unit,
    onFrameMoveFwdClicked: (frameNumber: Int) -> Unit,
    onPlayClicked: () -> Unit,
    onStopClicked: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color = Cl.mainGrey)
    ) {

        Column(
            Modifier.fillMaxSize()
        ) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(50.dp, 300.dp)
                    .wrapContentHeight()
                    .padding(50.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {

                modules.forEachIndexed { mIndex, module ->
                    ModuleUI(
                        modifier = Modifier
                            .weight(1f, fill = false),
                        leds = module.toUiLedEnabledMatrix(),
                        onClick = { row, column ->
                            onLedClicked.invoke(
                                mIndex,
                                row,
                                column
                            )
                        }
                    )
                }

            }

            // Timeline

            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(color = Cl.darkGrey)
            ) {

                Row(
                    Modifier
                        .wrapContentSize()
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp
                        )
                ) {

                    IconButton(
                        modifier = Modifier
                            .size(20.dp),
                        onClick = {
                            onAddFrameClicked.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource("svg/add.svg"),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.size(24.dp))

                    IconButton(
                        modifier = Modifier
                            .size(20.dp),
                        onClick = {
                            onFrameCopyClicked.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource("svg/copy.svg"),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.size(24.dp))

                    IconButton(
                        modifier = Modifier
                            .size(20.dp),
                        onClick = {
                            onDeleteFrameClicked.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource("svg/delete.svg"),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        modifier = Modifier
                            .size(20.dp),
                        onClick = {
                            onPlayClicked.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource("svg/play.svg"),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.size(24.dp))

                    IconButton(
                        modifier = Modifier
                            .size(20.dp),
                        onClick = {
                            onStopClicked.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource("svg/stop.svg"),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                }

                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {

                    frames.forEach { frame ->
                        item(key = frame.number) {
                            FrameUI(
                                frameItem = frame,
                                onFrameClicked = {
                                    onFrameClicked(frame.number)
                                },
                                onFrameTimeSelected = { time ->
                                    onFrameTimeSelected.invoke(
                                        frame.number,
                                        time
                                    )
                                },
                                onFrameMoveBwdClicked = {
                                    onFrameMoveBwdClicked.invoke(frame.number)
                                },
                                onFrameMoveFwdClicked = {
                                    onFrameMoveFwdClicked.invoke(frame.number)
                                }
                            )

                            Spacer(Modifier.size(8.dp))
                        }
                    }

                }

            }

        }

    }
}


fun main() = application {
    val vm = MainViewModel()

    Window(
        title = "Unnamed Project",
        onCloseRequest = ::exitApplication
    ) {

        window.minimumSize = Dimension(1080, 720)

        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item(
                    "New Project",
                    mnemonic = 'N',
                    onClick = {
                        vm.onNewProjectClicked()
                    },
                    icon = painterResource("svg/add.svg")
                )
                Item(
                    "Open",
                    mnemonic = 'O',
                    onClick = {

                    },
                    icon = painterResource("svg/open.svg")
                )
                Item(
                    "Save",
                    mnemonic = 'S',
                    onClick = {

                    },
                    icon = painterResource("svg/save.svg")
                )
                Item(
                    "Save as...",
                    onClick = {

                    },
                    icon = painterResource("svg/save_as.svg")
                )
                Item(
                    "Generate Arduino Sketch",
                    onClick = {
                        vm.onGenerateSketchClicked()
                    },
                    icon = painterResource("svg/save_as.svg")
                )
            }
        }


        App(vm)
    }
}
