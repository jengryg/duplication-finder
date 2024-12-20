package analyzer

import Logging
import analyzer.comparator.ComparatorRecord
import analyzer.comparator.CompartorMatcher
import logger
import model.IRecord
import model.RecordPersistence
import scanner.Indexer
import utils.FileIo
import kotlin.io.path.pathString

class ComparatorJob(
    private val jobName: String,
    private val sourcePath: String,
    private val targetPath: String
) : Logging {
    private val log = logger()

    private val jobDirectory = FileIo.ensureDirectoryExists("data/$jobName")
    private val sourceIndexJson = "${jobDirectory.pathString}/index-source.json"
    private val targetIndexJson = "${jobDirectory.pathString}/index-target.json"

    private val foundDirectoriesJson = "${jobDirectory.pathString}/compare-found-directories.json"
    private val foundFilesJson = "${jobDirectory.pathString}/compare-found-files.json"
    private val missingDirectoriesJson = "${jobDirectory.pathString}/compare-missing-directories.json"
    private val missingFilesJson = "${jobDirectory.pathString}/compare-missing-files.json"

    fun run() {
        log.atInfo()
            .setMessage("Starting compartor job.")
            .addKeyValue("jobName") { jobName }
            .addKeyValue("sourcePath") { sourcePath }
            .addKeyValue("targetPath") { targetPath }
            .log()

        val sourceRoot = Indexer(
            scanPath = sourcePath
        ).also { si ->
            si.run()
            RecordPersistence.save(sourceIndexJson, si.root)
        }.root

        val targetRoot = Indexer(
            scanPath = targetPath
        ).also { ti ->
            ti.run()
            RecordPersistence.save(targetIndexJson, ti.root)
        }.root

        val comparatorMatcher = CompartorMatcher(
            source = sourceRoot,
            target = targetRoot
        )

        val directoryResult = comparatorMatcher.directories { true }
        saveResults(directoryResult, foundDirectoriesJson, missingDirectoriesJson)

        val fileResult = comparatorMatcher.files { true }
        saveResults(fileResult, foundFilesJson, missingFilesJson)
    }

    private fun <T : IRecord> saveResults(results: List<ComparatorRecord<T>>, foundJson: String, missingJson: String) {
        val found = results.filter { it.matches.isNotEmpty() }
        val missing = results.filter { it.matches.isEmpty() }

        RecordPersistence.save(foundJson, found)
        RecordPersistence.save(missingJson, missing)
    }
}