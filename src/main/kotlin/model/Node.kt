package model

import org.apache.tika.Tika
import java.awt.Desktop
import java.io.File
import java.io.InputStream


interface Node {
    val label: String
}

val tika = Tika()

fun Node(file: File): Node {
    val contentType: String? = tika.detect(file.name)

    // Extend this with a mapping for other node types, e.g. zip files, and add a corresponding subclass of
    // FileSystemNode which lists its children
    return when {
        file.isDirectory -> FileSystemDirectory(file)
        contentType != null -> ContentReadableFileSystemFile(file, contentType)
        else -> FileSystemFile(file)
    }
}

interface Expandable {
    fun listChildren(): List<Node>
}

interface ContentReadable {
    val contentType: String
    fun contentInputStream(): InputStream
}

interface Openable {
    fun open()
}

private class FileSystemDirectory(val file: File) : Node, Expandable {
    override val label: String = file.name

    init {
        if (!file.isDirectory) {
            throw Exception("Cannot create a FileSystemDirectory Node for a File which doesn't represent a directory")
        }
    }

    override fun listChildren(): List<Node> {
        // Some directories can't be listed and will return null
        return file.listFiles()?.let { arrayOfFiles ->
            arrayOfFiles.sortWith(comparator = compareBy({ !it.isDirectory }, { it.name }))
            arrayOfFiles.map { Node(it) }
        }
            ?: emptyList()
    }
}

private open class FileSystemFile(val file: File) : Node, Openable {
    override val label: String = file.name

    init {
        if (file.isDirectory) {
            throw Exception("Cannot create a FileSystemFile Node for a File which represents a directory")
        }
    }

    override fun open() {
        Desktop.getDesktop().open(file)
    }
}

private class ContentReadableFileSystemFile(file: File, override val contentType: String) : FileSystemFile(file),
    ContentReadable {
    override fun contentInputStream(): InputStream {
        return file.inputStream()
    }
}