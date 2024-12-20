package analyzer.existence

import Logging
import logger
import model.DirectoryRecord
import model.FileRecord
import model.filterNoZeroBytes
import java.util.*

class ExistenceChecker(
    private val source: DirectoryRecord,
    private val target: DirectoryRecord,
) : Logging {
    private val log = logger()

    fun directories(
        directoryFilter: (DirectoryRecord) -> Boolean = { true },
    ): List<ExistenceRecord<DirectoryRecord>> {
        log.atInfo()
            .setMessage("Starting existence check for directories.")
            .addKeyValue("sourcePath") { source.path }
            .addKeyValue("targetPath") { target.path }
            .log()

        val sourceDirectories = source.flatDirectories().filterNoZeroBytes().filter { directoryFilter(it) }
        // use the flat list of all directories and ignore 0 bytes sized
        val targetFiles = target.flatFiles().filterNoZeroBytes().groupBy { it.groupId }

        val foundResults = mutableMapOf<UUID, Boolean>()
        val result = mutableListOf<ExistenceRecord<DirectoryRecord>>()

        val alreadyFound = mutableMapOf<UUID, Boolean>()

        sourceDirectories.forEach { record ->
            if (alreadyFound[record.id] != true) {
                val foundResult = findFiles(record.flatFiles(), targetFiles)
                result.add(
                    ExistenceRecord(
                        record = record,
                        exists = foundResult.first,
                        coverage = foundResult.second.map { it.path }
                    ).also {
                        log.atTrace()
                            .setMessage("Searched for record in target.")
                            .addKeyValue("directory") { it.path }
                            .addKeyValue("found") { it.exists }
                            .log()
                    }
                )
                if (foundResult.first) {
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

    fun files(
        fileFilter: (FileRecord) -> Boolean = { true },
    ): List<ExistenceRecord<FileRecord>> {
        log.atInfo()
            .setMessage("Starting existence check for files.")
            .addKeyValue("sourcePath") { source.path }
            .addKeyValue("targetPath") { target.path }
            .log()

        val sourceFiles = source.flatFiles().filterNoZeroBytes().filter { fileFilter(it) }
        val targetFiles = target.flatFiles().filterNoZeroBytes().groupBy { it.groupId }

        val result = mutableListOf<ExistenceRecord<FileRecord>>()

        sourceFiles.forEach { record ->
            val foundResult = findFiles(listOf(record), targetFiles)
            result.add(
                ExistenceRecord(
                    record = record,
                    exists = foundResult.first,
                    coverage = foundResult.second.map { it.path }
                ).also {
                    log.atTrace()
                        .setMessage("Searched for record in target.")
                        .addKeyValue("file") { it.path }
                        .addKeyValue("found") { it.exists }
                        .log()
                }
            )
        }

        return result.toList()
    }

    fun findFiles(
        files: List<FileRecord>,
        targetFiles: Map<String, List<FileRecord>>
    ): Pair<Boolean, List<FileRecord>> {
        val targetMatches = mutableListOf<FileRecord>()

        for (fileRecord in files) {
            val fileMatches = targetFiles[fileRecord.groupId] ?: emptyList()
            if (fileMatches.isEmpty()) {
                return Pair(false, emptyList())
            } else {
                targetMatches.addAll(fileMatches)
            }
        }

        return Pair(true, targetMatches)
    }
}