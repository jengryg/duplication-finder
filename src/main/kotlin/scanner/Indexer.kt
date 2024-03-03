package scanner

import Logging
import logger
import model.DirectoryRecord
import model.FileRecord
import utils.DigestCalculator
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

/**
 * Creates a [DirectoryRecord] of the given [scanPath] that contains a complete tree structure represented by
 * [DirectoryRecord] and [FileRecord] instances.
 *
 * The [DigestCalculator] is used to calculate the checksum of the byte content of each file found in the tree.
 * Be aware, that this can take a long time, depending on the total file size of the [scanPath].
 */
class Indexer(
    private val scanPath: String,
) : Logging {
    private val log = logger()

    /**
     * The [Path] representing the directory given by [scanPath].
     */
    private val directory: Path = Paths.get(scanPath).also {
        require(it.isDirectory()) { "Given scanPath must be a directory, but $scanPath is not." }
    }

    /**
     * The [DirectoryRecord] representing the directory given by [scanPath] as root node of the indexed records tree.
     */
    val root = DirectoryRecord(
        name = directory.name,
        path = directory.pathString
    ).also {
        log.atInfo()
            .setMessage("Initialized root record.")
            .addKeyValue("scanPath") { scanPath }
            .log()
    }

    /**
     * The directory road represents the current state of the indexer algorithm by managing the directories it found
     * but did not visit yet.
     */
    private val directoryRoad = DirectoryRoad()

    /**
     * Run the directory walker algorithm using the [directoryRoad] to traverse the file system from the [root] record
     * as starting point. This will build the records for all files and subdirectories as a tree structure.
     *
     * Note: Depending on the total size of the files in these directories and the number of directories and files,
     * this may take some time. Larger size and higher numbers take longer.
     */
    fun run() {
        log.atInfo()
            .setMessage("Starting directory indexing.")
            .addKeyValue("scanPath") { scanPath }
            .log()

        directoryRoad.init(root, directory)

        var directoryWalker = directoryRoad.next() ?: throw RuntimeException("Walker started without next candidate.")
        // get the first stop on the road, i.e. the root directory

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

            log.atDebug()
                .setMessage("Indexer progress.")
                .addKeyValue("nextCalls", directoryRoad.nextCalls)
                .addKeyValue("directoryCount", directoryRoad.directoryCount)
                .addKeyValue("fileCount", directoryRoad.fileCount)
                .addKeyValue("totalSize", directoryRoad.totalSize)
                .addKeyValue("remaining", directoryRoad.remaining)
                .addKeyValue("record", currentRecord)
                .log()

            directoryWalker = directoryRoad.next() ?: break
            // Let the walker advance to the next stop on the road. If this is the end of the road,
            // i.e. no stops are left, the call will return null and the loop will break.
        }

        root.update()
        // The root record is completely scanned now, so we can run the update on the root node to trigger a complete
        // update through the tree.
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

            log.atTrace()
                .setMessage("Visited Directory.")
                .addKeyValue("Record") { it }
                .log()
        }
    }

    private fun handleFile(parentRecord: DirectoryRecord, foundPath: Path) {
        directoryRoad.incFileCount()

        FileRecord(
            name = foundPath.name,
            path = foundPath.pathString,
            size = foundPath.fileSize(),
            hash = DigestCalculator.hash(foundPath)
        ).also {
            parentRecord.add(it)
            // add the newly found file as child to the parent DirectoryRecord containing it

            directoryRoad.addTotalSize(it.size)
            // account for the size of this file

            log.atTrace()
                .setMessage("Visited File.")
                .addKeyValue("Record") { it }
                .log()
        }
    }
}