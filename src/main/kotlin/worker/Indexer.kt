package worker

import Logging
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import logger
import model.DirectoryRecord
import model.FileRecord
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

/**
 * The [Indexer] is used to create a [DirectoryRecord] of the given [scanPath] that contains a complete tree structure
 * of [DirectoryRecord] and [FileRecord] instances representing the file system.
 *
 * If [hashing] is set to true, [DigestCalculator] is used to calculate the checksum of each file found in the tree.
 * See [DigestCalculator.hashFile] for more information on the hashing.
 */
class Indexer(
    private val scanPath: String,
    private val hashing: Boolean = false
) : Logging {
    private val log = logger()

    /**
     * The [Path] representing the given [scanPath].
     */
    private val directory = Paths.get(scanPath).also {
        assert(it.isDirectory())
    }

    /**
     * The [DirectoryRecord] representing the given [scanPath] as root node of the directory tree to index.
     */
    val root = DirectoryRecord(
        name = directory.name,
        path = directory.pathString
    ).also {
        log.atInfo()
            .setMessage("Initialized root record.")
            .addKeyValue("DirectoryRecord", it)
            .addKeyValue("scanPath", scanPath)
    }

    /**
     * The directory road represents the current state of the indexer algorithm by managing the directories it found
     * but did not visit yet.
     */
    private val directoryRoad = DirectoryRoad()

    /**
     * Run the directory walker algorithm to traverse the file system using the given [root] as starting point.
     * This will build the [DirectoryRecord] and [FileRecord] tree structure for the [root] record.
     *
     * Note: Depending on the number of directories and files and additionally, if [hashing] is enabled the sizes of
     * the files in the directories, this may take a long time.
     */
    fun run() {
        directoryRoad.init(root, directory)

        var directoryWalker = directoryRoad.next() ?: throw RuntimeException("Walker started without next candidate.")

        while (true) {
            val (currentRecord, currentPath) = directoryWalker

            Files.newDirectoryStream(currentPath).use { dirStream ->
                for (path in dirStream) {
                    when {
                        Files.isDirectory(path) -> handleDirectory(currentRecord, path)
                        Files.isRegularFile(path) -> handleFile(currentRecord, path)
                    }
                }
            }

            log.atInfo()
                .setMessage("Walker Progress")
                .addKeyValue("nextCalls", directoryRoad.nextCalls)
                .addKeyValue("directoryCount", directoryRoad.directoryCount)
                .addKeyValue("fileCount", directoryRoad.fileCount)
                .addKeyValue("totalSize", directoryRoad.totalSize)
                .addKeyValue("remaining", directoryRoad.remaining)
                .addKeyValue("record", currentRecord)
                .log()

            directoryWalker = directoryRoad.next() ?: break
            // let the walker advance to the next stop on the road
            // if this is the end of the road, i.e. no stops are left, the call will return null and the loop will break
        }

        root.update()
        // The root record is complete now, so we can run the update on the root node to trigger a complete update
        // through the tree.
    }

    private fun handleDirectory(parentRecord: DirectoryRecord, foundPath: Path) {
        directoryRoad.incDirectoryCount()

        DirectoryRecord(
            name = foundPath.name,
            path = foundPath.pathString
        ).also {
            parentRecord.add(it)
            // add the newly found directory as child to the parent DirectoryRecord containing it

            directoryRoad.addStop(it, foundPath)
            // add the newly found directory to the road our walker has to travel

            log.atDebug()
                .setMessage("Visited Directory")
                .addKeyValue("Record", it)
                .log()
        }
    }

    private fun handleFile(parentRecord: DirectoryRecord, foundPath: Path) {
        directoryRoad.incFileCount()

        FileRecord(
            name = foundPath.name,
            path = foundPath.pathString,
            ext = foundPath.extension,
            size = foundPath.fileSize(),
            hash = if (hashing) DigestCalculator.hashFile(foundPath) else ByteArray(32)
        ).also {
            parentRecord.add(it)
            // add the newly found file as child to the parent DirectoryRecord containing it

            directoryRoad.addTotalSize(it.size)
            // account for the size of this file

            log.atDebug()
                .setMessage("Visited File")
                .addKeyValue("Record", it)
                .log()
        }
    }
}