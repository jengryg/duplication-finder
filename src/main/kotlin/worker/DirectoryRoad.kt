package worker

import model.DirectoryRecord
import java.nio.file.Path

class DirectoryRoad {
    /**
     * The [directoryRoad] contains the pairs of [DirectoryRecord] and [Path] that need to be traversed by the
     * directory traversal algorithm.
     */
    private val directoryRoad: MutableList<Pair<DirectoryRecord, Path>> = mutableListOf()

    var nextCalls = 0L
        private set

    var directoryCount = 0L
        private set

    var fileCount = 0L
        private set

    var totalSize = 0L
        private set


    fun incDirectoryCount() {
        directoryCount++
    }

    fun incFileCount() {
        fileCount++
    }

    fun addTotalSize(numberOfBytes: Long) {
        totalSize += numberOfBytes
    }

    /**
     * Initialize the [DirectoryRecord] with the given [rootRecord] and [rootPath] as starting point.
     */
    fun init(rootRecord: DirectoryRecord, rootPath: Path) {
        directoryRoad.clear()
        directoryRoad.add(Pair(rootRecord, rootPath))

        directoryCount = 0L
        fileCount = 0L
        totalSize = 0L
    }

    /**
     * @return the next stop on the [DirectoryRoad] to visit, or null if there is no unvisited stop left.
     */
    fun next(): Pair<DirectoryRecord, Path>? {
        nextCalls++
        return directoryRoad.removeFirstOrNull()
    }

    fun addStop(record: DirectoryRecord, path: Path) {
        directoryRoad.add(Pair(record, path))
    }

    val remaining get(): Long = directoryRoad.size.toLong()
}