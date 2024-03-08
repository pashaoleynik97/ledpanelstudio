package com.github.pashaoleynik97.ledpanelstudio.utils

import com.github.pashaoleynik97.ledpanelstudio.data.MRow
import com.github.pashaoleynik97.ledpanelstudio.data.Module
import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes

fun Scopes.ProjectScope.toArduinoSketch(): String {

    return StringBuilder().apply {

        append("#include <MD_MAX72xx.h>")
        append(System.lineSeparator())
        append("#include <SPI.h>")
        append(System.lineSeparator())
        append(System.lineSeparator())

        append("#define HARDWARE_TYPE MD_MAX72XX::FC16_HW")
        append(System.lineSeparator())
        append("#define MAX_DEVICES $modulesCount")
        append(System.lineSeparator())
        append("#define CLK_PIN   13")
        append(System.lineSeparator())
        append("#define DATA_PIN  11")
        append(System.lineSeparator())
        append("#define CS_PIN    10")
        append(System.lineSeparator())
        append(System.lineSeparator())

        append("MD_MAX72XX mx = MD_MAX72XX(HARDWARE_TYPE, CS_PIN, MAX_DEVICES);")
        append(System.lineSeparator())
        append(System.lineSeparator())

        append("void setup()")
        append(System.lineSeparator())
        append("{")
        append(System.lineSeparator())
        append("mx.begin();")
        append(System.lineSeparator())
        append("mx.control(MD_MAX72XX::INTENSITY, 5);")
        append(System.lineSeparator())
        append("mx.clear();")
        append(System.lineSeparator())
        append("}")
        append(System.lineSeparator())
        append(System.lineSeparator())

        append("void lightLed(int module, int row, byte value) {")
        append(System.lineSeparator())
        append("  mx.setRow(module, row, value);")
        append(System.lineSeparator())
        append("}")
        append(System.lineSeparator())
        append(System.lineSeparator())

        scenes.forEach { scene ->
            append("void a${scene.name}() {")
            append(System.lineSeparator())
            scene.frames.forEachIndexed { frameIndex, frame ->
                val alteredFrame = if (direction == Scopes.ProjectScope.Direction.RTL) {
                    frame.reversed()
                } else frame
                alteredFrame.forEachIndexed { moduleIndex, module ->
                    append(module.toLedCommands(moduleIndex))
                }
                append(System.lineSeparator())
                append("  delay(${scene.framesTime[frameIndex]});")
                append("  mx.clear();")
                append(System.lineSeparator())
                append(System.lineSeparator())
            }
            append("}")
        }

        append("void loop() {")
        append(System.lineSeparator())
        append("  mx.clear();")
        append(System.lineSeparator())
        append(System.lineSeparator())

        scenes.forEach { scene ->
            append("  a${scene.name}();")
            append(System.lineSeparator())
            append(System.lineSeparator())
        }

        append(System.lineSeparator())
        append("}")




    }.toString()

}

private fun Module.toLedCommands(moduleIndex: Int): String {
    val rows = arrayOf(r0, r1, r2, r3, r4, r5, r6, r7)
    return StringBuilder().apply {
        rows.forEachIndexed { i, row ->
            append("  lightLed($moduleIndex, $i, ${row.toHex()});")
            append(System.lineSeparator())
        }
    }.toString()
}
private fun MRow.toHex(): String {
    val arr = arrayOf(c0, c1, c2, c3, c4, c5, c6, c7)
    return arr.toHex()
}

private fun Array<Boolean>.toHex(): String {
    val binaryString = joinToString(separator = "") { if (it) "1" else "0" }
    val hexString = Integer.toHexString(binaryString.toInt(2))
    return "0x$hexString"
}