import analyzer.AnalyzerJob
import utils.FileIo
import java.nio.file.Paths
import kotlin.io.path.isDirectory

fun main(args: Array<String>) {
    require(args.size in 2..3) {
        "Invalid Arguments. Use <job name> <path to scan or index file> [<log level>:INFO]. Received ${args.size} arguments: ${
            args.joinToString(
                " "
            )
        }"
    }

    setLoggingLevel(args.getOrElse(2) { "INFO" })

    val jobName = args[0].also {
        try {
            FileIo.ensureDirectoryExists("data/$it")
        } catch (ex: Exception) {
            throw IllegalArgumentException("Could not create the job directory data/$it", ex)
        }
    }

    val pathToScan = args[1].also {
        require(Paths.get(args[1]).isDirectory()) { "Path to scan must be an actual directory." }
    }

    val analyzerJob = AnalyzerJob(jobName, pathToScan)
    analyzerJob.run()
}