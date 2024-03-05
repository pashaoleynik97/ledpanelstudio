package com.github.pashaoleynik97.ledpanelstudio.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.pashaoleynik97.ledpanelstudio.ui.Cl
import com.github.pashaoleynik97.ledpanelstudio.ui.LSSection
import com.github.pashaoleynik97.ledpanelstudio.ui.LargeSwitch
import com.github.pashaoleynik97.ledpanelstudio.ui.ModuleUI

@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {

    val vmState = viewModel.state.value
    println(vmState)

    MaterialTheme {


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
                    viewModel.switchPresentation()
                }
            )

        }


    }

}

fun main() = application {

    val vm = MainViewModel()

    Window(onCloseRequest = ::exitApplication) {

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
