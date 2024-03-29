package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ModuleUI(
    modifier: Modifier,
    isThumbnail: Boolean = false,
    leds: List<Pair<Int, Int>>,
    onClick: ((row: Int, column: Int) -> Unit)? = null
) {

    Column (
        modifier
            .then(
                Modifier
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .background(color = Cl.moduleBlack)
            )
    ) {

        for (i in 0..7) {
            Row(Modifier.weight(1f)) {
                for (j in 0..7) {
                    Led(
                        modifier = Modifier.weight(1f),
                        enabled = leds.firstOrNull { it.first == i && it.second == j } != null,
                        isThumbnail = isThumbnail,
                        onClick = {
                            onClick?.invoke(i, j)
                        }
                    )
                }
            }
        }

    }

}

@Composable
private fun Led(
    modifier: Modifier,
    enabled: Boolean,
    isThumbnail: Boolean,
    onClick: (() -> Unit)? = null
) {

    val clickableModifier = Modifier.clickable {
        onClick?.invoke()
    }

    val nonClickableModifier = Modifier

    Canvas(
        modifier
            .aspectRatio(1f)
            .then(
                if (onClick != null) clickableModifier else nonClickableModifier
            )
    ) {
        if (isThumbnail) {
            drawRect(
                size = this.size,
                color = if (enabled) Cl.selection else Cl.moduleBlack,
                style = Fill
            )
        } else {
            drawCircle(
                radius = (this.size.height / 2f) - 1.dp.toPx(),
                color = if (enabled) Cl.selection else Color.DarkGray,
                style = if (enabled) Fill else Stroke(width = 1.dp.toPx())
            )
        }
    }
}