import analyzer.AnalyzerJob
import analyzer.ComparatorJob

class Program(private val keyArgs: KeyArgs) {
    fun main() {
        when (keyArgs.getOrThrow("type").lowercase()) {
            "duplicates" -> duplicates()
            "comparator" -> comparator()
            else -> require(false) {
                """
                Use type=<choice> argument to select the utility you want to use.
                Available options for type are:
                    duplicates: to search for duplicates inside the complete recursive structure of a given directory
                    comparator: to compare a source directory to a target directory
            """.trimIndent()
            }
        }
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
}