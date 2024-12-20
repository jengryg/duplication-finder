package analyzer

import Logging
import analyzer.duplicates.DuplicateFinder
import logger
import model.RecordPersistence
import analyzer.scanner.Indexer
import utils.FileIo
import kotlin.io.path.pathString

/**
 * Initialize the [AnalyzerJob] with the [jobName] that should be used as subdirectory to store the json files
 * containing the results of scanning [scanPath] for duplicates.
 */
class AnalyzerJob(
    private val jobName: String,
    private val scanPath: String
) : Logging {
    private val log = logger()

    private val jobDirectory = FileIo.ensureDirectoryExists("data/$jobName")
    private val indexJson = "${jobDirectory.pathString}/index.json"
    private val duplicateDirectoriesJson = "${jobDirectory.pathString}/duplicates-directories.json"
    private val duplicateFilesJson = "${jobDirectory.pathString}/duplicates-files.json"

    /**
     * Run the scanning to create and store and index file and process the resulting directory and file records to
     * find duplicates.
     *
     * Results are stored in the [jobDirectory].
     */
    fun run() {
        log.atInfo()
            .setMessage("Starting analyser job.")
            .addKeyValue("jobName") { jobName }
            .addKeyValue("scanPath") { scanPath }
            .log()

        val root = Indexer(
            scanPath = scanPath
        ).apply {
            run()
            RecordPersistence.save(indexJson, root)
        }.root

        val duplicateFinder = DuplicateFinder(root)

        val duplicateDirectories = duplicateFinder.directories { true }
        val duplicateFiles = duplicateFinder.files { true }

        RecordPersistence.save(duplicateDirectoriesJson, duplicateDirectories)
        RecordPersistence.save(duplicateFilesJson, duplicateFiles)

        log.atInfo()
            .setMessage("Analyser job finished.")
            .addKeyValue("index") { indexJson }
            .addKeyValue("duplicatedDirectories") { duplicateDirectoriesJson }
            .addKeyValue("duplicateFiles") { duplicateFilesJson }
            .log()
    }
}