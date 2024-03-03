import ch.qos.logback.classic.Level
import model.DirectoryRecord
import model.RecordPersistence
import worker.DuplicateFinder
import scanner.Indexer

fun main(args: Array<String>) {
    setLoggingLevel(Level.TRACE)

    val sourceDirectoryPath = "C:\\DATA"
    // what directory to create the index from

    val outFile = sourceDirectoryPath.replace("\\", "__").replace(":", "_")
    // use the sourceDirectoryPath as name component for the outFiles

    val indexer = Indexer(scanPath = sourceDirectoryPath)
    indexer.run()

    RecordPersistence.save("data\\index_$outFile.json", indexer.root)

    val root = RecordPersistence.load<DirectoryRecord>("data\\index_$outFile.json")

    val duplicateFinder = DuplicateFinder(root = root)

    duplicateFinder.directories("data\\duplicate_directories_$outFile.csv") {
        true
    }

    duplicateFinder.files("data\\duplicate_files_$outFile.csv") {
        true
    }
}