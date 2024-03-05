package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ModuleUI(
    leds: List<Pair<Int, Int>>
) {

    Column (
        Modifier
            .size(80.dp)
            .background(color = Color.Black)
    ) {

        for (i in 0..7) {
            Row {
                for (j in 0..7) {
                    Led(enabled = leds.firstOrNull { it.first == j && it.second == i } != null)
                }
            }
        }

    }

}

@Composable
private fun Led(
    enabled: Boolean
) {
    Canvas(
        Modifier.size(10.dp)
    ) {
        drawCircle(
            radius = 4.dp.toPx(),
            color = Color.Blue,
            style = if (enabled) Fill else Stroke(width = 1.dp.toPx())
        )
    }
}