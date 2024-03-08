package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.pashaoleynik97.ledpanelstudio.main.data.FrameItem
import com.github.pashaoleynik97.ledpanelstudio.main.toUiLedEnabledMatrix

@Composable
fun FrameUI(
    modifier: Modifier = Modifier,
    frameItem: FrameItem,
    onFrameClicked: () -> Unit,
    onFrameTimeSelected: (time: Long) -> Unit,
    onFrameMoveFwdClicked: () -> Unit,
    onFrameMoveBwdClicked: () -> Unit
) {

    var timeMenuExpanded by remember { mutableStateOf(false) }

    val times: Set<Long> = remember {
        setOf(
            20L,
            25L,
            50L,
            100L,
            125L,
            150L,
            200L,
            250L,
            300L,
            500L,
            1000L,
            1250L,
            1500L,
            2000L,
            2500L,
            3000L,
            4000L,
            5000L
        )
    }

    Row(
        modifier = modifier
            .then(
                Modifier
                    .height(150.dp)
                    .wrapContentWidth()
            )
    ) {

        if (frameItem.bwdAvailable) {

            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(44.dp)
                    .drawBehind {
                        drawCircle(
                            color = Cl.mainGrey,
                            radius = size.height / 2f,
                            center = Offset(x = size.height / 2f, y = size.height / 2f)
                        )
                    }.clickable {
                        onFrameMoveBwdClicked.invoke()
                    }.clip(
                        RectangleShape
                    ).align(
                        Alignment.CenterVertically
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.Center),
                    painter = painterResource("svg/arrow_bwd.svg"),
                    contentDescription = null,
                    tint = Color.White
                )
            }

        }

        Column(
            Modifier
                .fillMaxHeight()
                .aspectRatio(1.25f, matchHeightConstraintsFirst = true)
                .zIndex(1f)
        ) {

            // Preview + Num
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Cl.light)
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(50.dp, 300.dp)
                        .wrapContentHeight()
                        .padding(16.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center
                ) {

                    frameItem.modules.forEach { module ->
                        ModuleUI(
                            modifier = Modifier
                                .weight(1f, fill = false),
                            leds = module.toUiLedEnabledMatrix(),
                            isThumbnail = true
                        )
                    }

                }

                Box(
                    Modifier
                        .wrapContentWidth()
                        .height(30.dp)
                        .background(color = if (frameItem.selected) Cl.selection else Cl.mainGrey)
                ) {

                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 8.dp)
                            .align(Alignment.Center),
                        text = frameItem.displayNumber.toString(),
                        fontSize = 12.sp,
                        color = Color.White
                    )

                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable { onFrameClicked.invoke() }
                )

            }

            // Duration
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Cl.mainGrey)
            ) {

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(vertical = 12.dp),
                    text = "${frameItem.duration} ms",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Box(
                    Modifier
                        .wrapContentSize()
                        .padding(end = 8.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        onClick = {
                            timeMenuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier
                            .height(250.dp),
                        expanded = timeMenuExpanded,
                        onDismissRequest = { timeMenuExpanded = false }
                    ) {
                        times.forEach{ time ->
                            DropdownMenuItem(
                                onClick = {
                                    timeMenuExpanded = false
                                    onFrameTimeSelected.invoke(time)
                                }
                            ) {
                                Text(
                                    text = "$time ms",
                                    color = if (frameItem.duration == time) Cl.selection else Color.White
                                )
                            }
                        }
                    }
                }

            }

        }

        if (frameItem.fwdAvailable) {

            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(44.dp)
                    .drawBehind {
                        drawCircle(
                            color = Cl.mainGrey,
                            radius = size.height / 2f,
                            center = Offset(x = 0f, y = size.height / 2f)
                        )
                    }.clickable {
                        onFrameMoveFwdClicked.invoke()
                    }.clip(
                        RectangleShape
                    ).align(
                        Alignment.CenterVertically
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.Center),
                    painter = painterResource("svg/arrow_fwd.svg"),
                    contentDescription = null,
                    tint = Color.White
                )
            }

        }

    }

}