package utils

import Logging
import logger
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

object FileIo : Logging {
    private val log = logger()

    /**
     * Gets the entire content of the given [file] as a String using [StandardCharsets.UTF_8].
     * It's not recommended to use this function on huge files.
     *
     * @param file the path to the file including the file name and extension
     *
     * @return the entire content of the file
     */
    fun readFile(file: String): String {
        return Paths.get(file).readText(StandardCharsets.UTF_8).also {
            log.atInfo()
                .setMessage("Reading File.")
                .addKeyValue("file") { file }
                .addKeyValue("chars") { it.length }
                .log()
        }
    }

    /**
     * Sets the content of the given [file] to the string given as [content] using [StandardCharsets.UTF_8].
     * Create a new file if it does not exist.
     * If the file already exists, its contents will be overwritten.
     *
     * @param file the path to the file including the file name and extension
     * @param content the entire content the file should contain when this operation is done
     */
    fun writeFile(file: String, content: String?) {
        Paths.get(file).writeText(
            content ?: "",
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )

        log.atInfo()
            .setMessage("Saved File.")
            .addKeyValue("file") { file }
            .log()
    }

    /**
     * If the given path is not a directory, this method will try to create it and all of it parent directories.
     */
    fun ensureDirectoryExists(path: String): Path {
        return Paths.get(path).also {
            if (!it.isDirectory()) {
                it.createDirectories()
            }
        }
    }
}