package com.github.pashaoleynik97.ledpanelstudio.utils

import com.github.pashaoleynik97.ledpanelstudio.misc.Scopes
import java.io.*

fun saveProject(project: Scopes.ProjectScope, pathToSave: String) {
    writeObjectToFile(project, pathToSave)
}

fun openProject(path: String): Scopes.ProjectScope {
    return readObjectFromFile(path)
}

fun saveSketch(sketch: String, pathToSave: String) {
    writeStringToFile(sketch, pathToSave)
}

private fun writeStringToFile(string: String, filePath: String) {
    File(filePath).printWriter().use { outputStream ->
        outputStream.print(string)
    }
}

private fun <T> writeObjectToFile(obj: T, filePath: String) {
    ObjectOutputStream(FileOutputStream(filePath)).use { outputStream ->
        outputStream.writeObject(obj)
    }
}

private fun <T> readObjectFromFile(filePath: String): T {
    ObjectInputStream(FileInputStream(filePath)).use { inputStream ->
        @Suppress("UNCHECKED_CAST")
        return inputStream.readObject() as T
    }
}