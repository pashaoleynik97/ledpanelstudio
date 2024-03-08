package com.github.pashaoleynik97.ledpanelstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ToolButton(
    modifier: Modifier,
    resourcePath: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .then(
                Modifier
                    .aspectRatio(1f)
                    .background(color = if (enabled) Cl.selection else Color.Transparent, shape = RoundedCornerShape(10.dp))
                    .clickable { onClick.invoke() }
                    .clip(RoundedCornerShape(10.dp))
            )
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            painter = painterResource(resourcePath = resourcePath),
            contentDescription = null,
            tint = Color.White
        )
    }

}