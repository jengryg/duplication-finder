package analyzer.comparator

import Logging
import logger
import model.DirectoryRecord
import model.FileRecord
import model.filterNoZeroBytes
import java.util.UUID
import kotlin.collections.isNotEmpty
import kotlin.collections.map

class CompartorMatcher(
    private val source: DirectoryRecord,
    private val target: DirectoryRecord
) : Logging {
    private val log = logger()

    /**
     * Process the [source] [DirectoryRecord] and [target] [DirectoryRecord] of this instance to find all directories
     * that exist in source and also at least one time in target.
     * The matching uses the [DirectoryRecord.groupId] to compare the records.
     *
     * @return a list of [ComparatorRecord] representing the outcome for all [DirectoryRecord] in the source.
     */
    fun directories(
        directoryFilter: (DirectoryRecord) -> Boolean = { true },
    ): List<ComparatorRecord<DirectoryRecord>> {
        log.atInfo()
            .setMessage("Starting search for directories.")
            .addKeyValue("sourcePath") { source.path }
            .addKeyValue("targetPath") { target.path }
            .log()

        val sourceDirectories = source.flatDirectories().filterNoZeroBytes().filter { directoryFilter(it) }
        // use the flat list of all directories and ignore 0 bytes sized
        val targetDirectories = target.flatDirectories().filterNoZeroBytes().groupBy { it.groupId }

        val foundResults = mutableMapOf<UUID, Boolean>()
        val result = mutableListOf<ComparatorRecord<DirectoryRecord>>()

        sourceDirectories.forEach { record ->
            val targetMatches = targetDirectories[record.groupId] ?: emptyList()
            if (foundResults[record.id] != true) {
                // this record was not yet processed before
                result.add(
                    ComparatorRecord(
                        record = record,
                        matches = targetMatches.map { it.path }
                    ).also {
                        log.atTrace()
                            .setMessage("Processed record from source.")
                            .addKeyValue("directory") { it.path }
                            .addKeyValue("matches") { it.matches }
                            .log()
                    }
                )
                if (targetMatches.isNotEmpty()) {
                    record.flatDirectories().forEach { child ->
                        foundResults[child.id] = true
                        // If a source directory is found in target, all of its children directories have also been
                        // found. We only want to have the top-most match directory of the tree in our results.
                    }
                }
            }
        }

        return result.toList()
    }

    /**
     * Process the [source] [DirectoryRecord] and [target] [DirectoryRecord] of this instance to find all files that
     * exist in source and also at least one time in target.
     * The matching uses the [FileRecord.groupId] to compare the records.
     *
     * @return a list of [ComparatorRecord] representing the outcome for all [FileRecord] in the source.
     */
    fun files(
        fileFilter: (FileRecord) -> Boolean = { true },
    ): List<ComparatorRecord<FileRecord>> {
        log.atInfo()
            .setMessage("Starting search for files.")
            .addKeyValue("sourcePath") { source.path }
            .addKeyValue("targetPath") { target.path }
            .log()

        val sourceFiles = source.flatFiles().filterNoZeroBytes().filter { fileFilter(it) }
        // use the flat list of all files and ignore 0 byte sized
        val targetFiles = target.flatFiles().filterNoZeroBytes().groupBy { it.groupId }

        val result = mutableListOf<ComparatorRecord<FileRecord>>()

        sourceFiles.forEach { record ->
            val targetMatches = targetFiles[record.groupId] ?: emptyList()
            result.add(
                ComparatorRecord(
                    record = record,
                    matches = targetMatches.map { it.path }
                ).also {
                    log.atTrace()
                        .setMessage("Processed record from source.")
                        .addKeyValue("file") { it.path }
                        .addKeyValue("matches") { it.matches }
                        .log()
                }
            )
        }

        return result.toList()
    }
}