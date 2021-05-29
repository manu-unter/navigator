package model

import java.io.File
import java.io.InputStream
import java.net.URLConnection


interface Node {
    val label: String
}

fun Node(file: File): Node {
    // This method of inferring the content type is rather limited - it will only identify basic file extensions.
    // We could extend it with https://tika.apache.org/ to also support Markdown etc.
    val contentType: String? = URLConnection.guessContentTypeFromName(file.name)

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

private open class FileSystemFile(val file: File) : Node {
    override val label: String = file.name

    init {
        if (file.isDirectory) {
            throw Exception("Cannot create a FileSystemFile Node for a File which represents a directory")
        }
    }
}

private class ContentReadableFileSystemFile(file: File, override val contentType: String) : FileSystemFile(file),
    ContentReadable {
    override fun contentInputStream(): InputStream {
        return file.inputStream()
    }
}