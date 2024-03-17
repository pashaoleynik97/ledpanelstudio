package com.github.pashaoleynik97.ledpanelstudio.data

import java.io.Serializable
import java.util.UUID

data class Scene(
    val name: String,
    val frames: List<List<Module>>,
    val framesTime: List<Long>,
    val sceneId: String = UUID.randomUUID().toString(),
    val iterations: Int = 1
): Serializable

data class Module(
    val ordinal: Int,
    val r0: MRow = MRow(),
    val r1: MRow = MRow(),
    val r2: MRow = MRow(),
    val r3: MRow = MRow(),
    val r4: MRow = MRow(),
    val r5: MRow = MRow(),
    val r6: MRow = MRow(),
    val r7: MRow = MRow()
): Serializable

data class MRow(
    val c0: Boolean = false,
    val c1: Boolean = false,
    val c2: Boolean = false,
    val c3: Boolean = false,
    val c4: Boolean = false,
    val c5: Boolean = false,
    val c6: Boolean = false,
    val c7: Boolean = false
): Serializable