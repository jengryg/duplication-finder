package scanner

import model.DirectoryRecord
import java.nio.file.Path

/**
 * DirectoryRoad models the travel through the directory tree on the file system to create the index using the [Indexer].
 *
 * Use [init] to initialize the road.
 * Use [next] to obtain the next stop on the road that has to be indexed.
 * Use [addStop] to add additional directories as stops.
 */
class DirectoryRoad {
    /**
     * The [road] contains the pairs of [DirectoryRecord] and [Path] that need to be traversed by the directory
     * traversal algorithm.
     */
    private val road: MutableList<Pair<DirectoryRecord, Path>> = mutableListOf()

    val remaining: Int get() = road.size

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
        road.clear()
        road.add(
            Pair(rootRecord, rootPath)
        )
    }

    /**
     * @return the next stop on the [DirectoryRoad] to visit, or null if there is no unvisited stop left.
     */
    fun next(): Pair<DirectoryRecord, Path>? {
        nextCalls++
        return road.removeFirstOrNull()
    }

    /**
     * Add another stop to the road.
     *
     * @param record representing the directory that we add as stop
     * @param path the path referencing the directory that we add as stop
     */
    fun addStop(record: DirectoryRecord, path: Path) {
        road.add(
            Pair(record, path)
        )
    }
}