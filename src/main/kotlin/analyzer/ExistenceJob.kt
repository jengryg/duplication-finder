package analyzer

import Logging
import analyzer.existence.ExistenceChecker
import logger
import model.DirectoryRecord
import model.RecordPersistence
import utils.FileIo
import kotlin.io.path.pathString

class ExistenceJob(
    private val jobName: String,
    outputName: String,
    sourceIndex: String,
    targetIndex: String,
) : Logging {
    private val log = logger()

    private val jobDirectory = FileIo.ensureDirectoryExists("data/$jobName")
    private val outputDirectoryJson = "${jobDirectory.pathString}/$outputName-directory.json"
    private val outputFileJson = "${jobDirectory.pathString}/$outputName-file.json"
    private val sourceIndexJson = "${jobDirectory.pathString}/$sourceIndex.json"
    private val targetIndexJson = "${jobDirectory.pathString}/$targetIndex.json"

    fun run() {
        log.atInfo()
            .setMessage("Starting existence job.")
            .addKeyValue("jobName") { jobName }
            .addKeyValue("sourceJson") { sourceIndexJson }
            .addKeyValue("targetJson") { targetIndexJson }
            .log()

        val sourceRoot = RecordPersistence.load<DirectoryRecord>(sourceIndexJson).also {
            log.atInfo()
                .setMessage("Loaded and deserialized source json.")
                .addKeyValue("record") { it.path }
                .log()
        }
        val targetRoot = RecordPersistence.load<DirectoryRecord>(targetIndexJson).also {
            log.atInfo()
                .setMessage("Loaded and deserialized target json.")
                .addKeyValue("record") { it.path }
                .log()
        }

        val existenceChecker = ExistenceChecker(
            source = sourceRoot,
            target = targetRoot,
        )

        val directoryResults = existenceChecker.directories { true }
        RecordPersistence.save(outputDirectoryJson, directoryResults)
        val fileResults = existenceChecker.files { true }
        RecordPersistence.save(outputFileJson, fileResults)
    }
}