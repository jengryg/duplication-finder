import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import persistence.DirectoryRecordPersistence
import worker.DuplicateFinder
import worker.Indexer

fun main(args: Array<String>) {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    rootLogger.level = Level.INFO

    val sourceDirectoryPath = "C:\\DATA"
    // what directory to create the index from

    val outFile = sourceDirectoryPath.replace("\\", "__").replace(":", "_")
    // use the sourceDirectoryPath as name component for the outFiles

    val indexer = Indexer(scanPath = sourceDirectoryPath, hashing = true)
    indexer.run()

    DirectoryRecordPersistence.save("data\\index_$outFile.json", indexer.root)

    val root = DirectoryRecordPersistence.load("data\\index_$outFile.json")

    val duplicateFinder = DuplicateFinder(root = root)

    duplicateFinder.directories("data\\duplicate_directories_$outFile.csv") {
        true
    }

    duplicateFinder.files("data\\duplicate_files_$outFile.csv") {
        true
    }
}