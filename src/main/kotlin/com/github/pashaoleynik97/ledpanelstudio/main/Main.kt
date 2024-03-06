package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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

    val uiCallbacks = remember {
        object : MainViewModel.UiCallbacks {
            override fun showSceneDeleteDialog(sceneName: String) {
                showDeleteDialog = Pair(true, sceneName)
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
                        .weight(1f)
                )

                ToolsPane()

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

                    Spacer(Modifier.height(16.dp))

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Cl.darkGrey
                    )

                    Spacer(Modifier.height(16.dp))

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
                    color = Color.White
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(60.dp)
            .fillMaxHeight()
            .background(color = Cl.lightGrey)
    ) {

    }

}

@Composable
private fun MainPane(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color = Cl.mainGrey)
    ) {

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
                    "Exit",
                    onClick = {

                    },
                )
            }
        }


        App(vm)
    }
}
