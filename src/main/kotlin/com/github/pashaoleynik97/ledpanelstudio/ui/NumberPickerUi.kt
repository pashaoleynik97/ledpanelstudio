package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPickerUi(
    modifier: Modifier = Modifier.wrapContentSize(),
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {

    Row(modifier = modifier) {

        Spacer(Modifier.size(8.dp))

        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterVertically),
            content = {
                Icon(
                    painter = painterResource(resourcePath = "svg/remove.svg"),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        Text(
            modifier = Modifier
                .width(50.dp)
                .wrapContentHeight()
                .align(Alignment.CenterVertically),
            text = value.toString(),
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterVertically),
            content = {
                Icon(
                    painter = painterResource(resourcePath = "svg/add.svg"),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        )

        Spacer(Modifier.size(8.dp))

    }

}