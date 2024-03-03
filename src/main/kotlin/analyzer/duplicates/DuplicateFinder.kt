package analyzer.duplicates

import Logging
import logger
import model.DirectoryRecord
import model.FileRecord
import model.determineGroups
import model.filterNoZeroBytes
import java.util.*

class DuplicateFinder(
    private val root: DirectoryRecord
) : Logging {
    private val log = logger()

    /**
     * Process the [root] [DirectoryRecord] of this instance to find all directory duplicates using the
     * [DirectoryRecord.groupId].
     *
     * @return a list of [DuplicateRecord] representing all found duplicated [DirectoryRecord].
     */
    fun directories(
        directoryFilter: (DirectoryRecord) -> Boolean = { true },
    ): List<DuplicateRecord<DirectoryRecord>> {
        log.atInfo()
            .setMessage("Starting search for duplicated directories.")
            .addKeyValue("rootPath") { root.path }
            .log()


        val directories =
            root.flatDirectories().filterNoZeroBytes().filter { directoryFilter(it) }
        // use the flat list of all directories and ignore 0 byte sized
        val duplicates =
            directories.determineGroups().filter { it.value.size > 1 }
        // ignore single element groups, since these contain no duplicated records

        log.atInfo()
            .setMessage("Duplicated DirectoryRecords determined.")
            .addKeyValue("rootPath") { root.path }
            .addKeyValue("duplicateCount") { duplicates.size }
            .log()

        val foundDuplicates = mutableMapOf<UUID, Boolean>()
        val result = mutableListOf<DuplicateRecord<DirectoryRecord>>()

        duplicates.forEach { (_, dupes) ->
            if (dupes.any { foundDuplicates[it.id] != true }) {
                // this duplicate has not been noticed before

                result.add(
                    DuplicateRecord(
                        record = dupes.first(),
                        duplicates = dupes.map { it.path }
                    ).also {
                        log.atTrace()
                            .setMessage("Duplicated Directory recorded.")
                            .addKeyValue("directory") { it.path }
                            .addKeyValue("duplications") { it.duplicates }
                            .log()
                    }
                )

                dupes.forEach { dup ->
                    dup.flatDirectories().forEach { child ->
                        foundDuplicates[child.id] = true
                        // If a parent directory has a duplicate, all of its child directories also have a duplicate in
                        // the parents duplicate. We only want to have the top-most duplicate directories of the tree in
                        // our results.
                    }
                }
            }
        }

        return result.toList()
    }

    /**
     * Process the root [DirectoryRecord] of this instance to find all file duplicates using the
     * [FileRecord.groupId].
     *
     * @return a list of [DuplicateRecord] representing all found duplicated [FileRecord].
     */
    fun files(
        fileFilter: (FileRecord) -> Boolean = { true },
    ): List<DuplicateRecord<FileRecord>> {
        log.atInfo()
            .setMessage("Starting search for duplicated files.")
            .addKeyValue("rootPath") { root.path }
            .log()

        val files = root.flatFiles().filterNoZeroBytes().filter { fileFilter(it) }
        // use the flat list of all files and ignore 0 byte sized

        val duplicates = files.determineGroups().filter { it.value.size > 1 }
        // ignore single element groups, since these contain no duplicated records

        log.atInfo()
            .setMessage("Duplicated FileRecords determined.")
            .addKeyValue("rootPath") { root.path }
            .addKeyValue("duplicateCount") { duplicates.size }
            .log()

        val result = mutableListOf<DuplicateRecord<FileRecord>>()

        duplicates.forEach { (_, dupes) ->
            result.add(
                DuplicateRecord(
                    record = dupes.first(),
                    duplicates = dupes.map { it.path }
                ).also {
                    log.atTrace()
                        .setMessage("Duplicated File recorded.")
                        .addKeyValue("file") { it.path }
                        .addKeyValue("duplications") { it.duplicates }
                        .log()
                }
            )
        }

        return result.toList()
    }
}