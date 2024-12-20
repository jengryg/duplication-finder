package analyzer

import Logging
import analyzer.scanner.Indexer
import logger
import model.RecordPersistence
import utils.FileIo
import kotlin.io.path.pathString

class ScannerJob(
    private val jobName: String,
    private val indexName: String,
    private val scanPath: String
) : Logging {
    private val log = logger()

    private val jobDirectory = FileIo.ensureDirectoryExists("data/$jobName")
    private val indexJson = "${jobDirectory.pathString}/$indexName.json"

    fun run() {
        log.atInfo()
            .setMessage("Starting scanner job.")
            .addKeyValue("jobName") { jobName }
            .addKeyValue("indexName") { indexName }
            .addKeyValue("scanPath") { scanPath }
            .log()

        val indexer = Indexer(
            scanPath = scanPath,
        )

        indexer.run()

        RecordPersistence.save(indexJson, indexer.root)

        log.atInfo()
            .setMessage("Index json file created.")
            .addKeyValue("jobName") { jobName }
            .addKeyValue("indexName") { indexName }
            .addKeyValue("scanPath") { scanPath }
            .addKeyValue("indexJson") { indexJson }
            .log()
    }
}