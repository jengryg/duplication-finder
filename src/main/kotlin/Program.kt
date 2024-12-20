import analyzer.AnalyzerJob
import analyzer.ComparatorJob
import analyzer.ExistenceJob
import analyzer.ScannerJob

class Program(private val keyArgs: KeyArgs) {
    fun main() {
        when (keyArgs.getOrThrow("type").lowercase()) {
            "indexer" -> indexer()
            "duplicates" -> duplicates()
            "comparator" -> comparator()
            "existence" -> existence()
            else -> require(false) {
                """
                Use type=<choice> argument to select the utility you want to use.
                Available options for type are:
                    indexer: create a json directory tree for the given directory
                    duplicates: to search for duplicates inside a json directory tree
                    comparator: to compare a source json directory tree to a target json directory tree
                    existence: check if the files in the source json directory tree also exist in the target directory tree
            """.trimIndent()
            }
        }
    }

    private fun indexer() {
        val scannerJob = ScannerJob(
            jobName = keyArgs.getOrThrow("jobName"),
            indexName = keyArgs.getOrThrow("indexName"),
            scanPath = keyArgs.getOrThrow("scanPath"),
        )
        scannerJob.run()
    }

    private fun duplicates() {
        val analyzerJob = AnalyzerJob(
            jobName = keyArgs.getOrThrow("jobName"),
            scanPath = keyArgs.getOrThrow("scanPath"),
        )
        analyzerJob.run()
    }

    private fun comparator() {
        val comparatorJob = ComparatorJob(
            jobName = keyArgs.getOrThrow("jobName"),
            sourcePath = keyArgs.getOrThrow("sourcePath"),
            targetPath = keyArgs.getOrThrow("targetPath"),
        )
        comparatorJob.run()
    }

    private fun existence() {
        val existenceJob = ExistenceJob(
            jobName = keyArgs.getOrThrow("jobName"),
            outputName = keyArgs.getOrThrow("outputName"),
            sourceIndex = keyArgs.getOrThrow("sourceIndex"),
            targetIndex = keyArgs.getOrThrow("targetIndex"),
        )
        existenceJob.run()
    }
}