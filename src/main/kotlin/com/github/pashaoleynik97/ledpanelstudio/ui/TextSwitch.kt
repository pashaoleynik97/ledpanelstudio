package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LSSection(
    val caption: String,
    val selected: Boolean,
    val tag: Any
)

@Composable
fun LargeSwitch(
    modifier: Modifier = Modifier,
    sections: List<LSSection>,
    onSectionClicked: (Any) -> Unit
) {

    Box(
        modifier = modifier
            .then(
                Modifier
                    .wrapContentWidth()
                    .background(color = Cl.lightGrey, shape = RoundedCornerShape(10.dp))
            )
    ) {

        Row(
            Modifier
                .padding(3.dp)
                .fillMaxHeight()
                .wrapContentWidth()
        ) {

            sections.forEach { section ->
                Box(
                    Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .background(color = if (section.selected) Cl.selection else Color.Transparent, shape = RoundedCornerShape(7.dp))
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onSectionClicked(section.tag) }
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(
                                horizontal = 30.dp
                            ),
                        text = section.caption,
                        color = Color.White
                    )
                }
            }

        }

    }

}

@Composable
fun NormalSwitch(
    modifier: Modifier = Modifier,
    sections: List<LSSection>,
    onSectionClicked: (Any) -> Unit
) {

    Box(
        modifier = modifier
            .then(
                Modifier
                    .wrapContentWidth()
                    .background(color = Cl.lightGrey, shape = RoundedCornerShape(10.dp))
            )
    ) {

        Row(
            Modifier
                .padding(3.dp)
                .wrapContentHeight()
                .wrapContentWidth()
        ) {

            sections.forEach { section ->
                Box(
                    Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .background(color = if (section.selected) Cl.selection else Color.Transparent, shape = RoundedCornerShape(7.dp))
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onSectionClicked(section.tag) }
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(
                                horizontal = 8.dp
                            ),
                        text = section.caption,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

        }

    }

}