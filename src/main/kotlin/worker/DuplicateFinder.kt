package worker

import Logging
import logger
import model.DirectoryRecord
import model.FileRecord
import model.RecordAbstract
import org.apache.commons.csv.CSVFormat
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import java.util.UUID

class DuplicateFinder(
    private val root: DirectoryRecord
) : Logging {
    private val log = logger()

    fun directories(reportCsvFile: String, directoryFilter: (DirectoryRecord) -> Boolean) {
        val directories = root.flatDirectories().filter { it.size != 0L }.filter { directoryFilter(it) }
        // use the flat list of all directories and ignore 0 byte sized
        // then apply the directoryFilter

        val duplicates = directories.groupBy { groupId(it) }.filter { it.value.size > 1 }
        // use groupBy with the calculated groupId to match records that we consider duplicates
        // ignore single element groups, since these contain no duplicated records

        val subdirectoryFilteredKeys = mutableListOf<String>()
        val alreadyDuped = mutableMapOf<UUID, Boolean>()

        duplicates.forEach { (key, dupes) ->
            if (dupes.any { alreadyDuped[it.id] != true }) {
                subdirectoryFilteredKeys.add(key)
                // this duplicate has not been noticed before

                dupes.forEach { duplicate ->
                    duplicate.flatDirectories().forEach { child ->
                        alreadyDuped[child.id] = true
                        // All subdirectories of these duplicate records are now already known, there is no need to have
                        // them included in the list another time.
                        // If two directories are duplicates, it implies that the contents of the directories are also
                        // all duplicated.
                    }
                }
            }
        }

        val map = createResultMap(subdirectoryFilteredKeys, duplicates)
        val df = map.toDataFrame()
        df.writeCSV(reportCsvFile, CSVFormat.DEFAULT)

    }

    fun files(reportCsvFile: String, fileFilter: (FileRecord) -> Boolean) {
        val files = root.flatFiles().filter { it.size != 0L }.filter { fileFilter(it) }
        // use the flat list of all files and ignore 0 byte sized
        // then apply the fileFilter

        val duplicates = files.groupBy { groupId(it) }.filter { it.value.size > 1 }
        // use groupBy with the calculated groupId to match records that we consider duplicates
        // ignore single element groups, since these contain no duplicated records

        val map = createResultMap(duplicates.keys, duplicates)
        val df = map.toDataFrame()
        df.writeCSV(reportCsvFile, CSVFormat.DEFAULT)
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T : RecordAbstract> createResultMap(
        keys: Iterable<String>,
        duplicates: Map<String, List<T>>
    ): Map<String, List<String?>> {
        return mapOf(
            "hash" to keys.map { duplicates[it]?.first()?.hash?.toHexString() },
            "size" to keys.map { duplicates[it]?.first()?.size.toString() },
            "dupes" to keys.map { duplicates[it]?.size.toString() }, // how many copies do we have of this
            "paths" to keys.map { duplicates[it]?.joinToString("\r\n") { r -> r.path } }
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun groupId(directoryRecord: DirectoryRecord): String {
        return "${directoryRecord.hash.toHexString(HexFormat.UpperCase)}-${
            directoryRecord.size.toString().padStart(
                BYTE_PADDING_IN_DECIMAL
            )
        }"
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun groupId(fileRecord: FileRecord): String {
        return "${fileRecord.hash.toHexString(HexFormat.UpperCase)}-${
            fileRecord.size.toString().padStart(
                BYTE_PADDING_IN_DECIMAL
            )
        }"
    }

    companion object {
        /**
         * Set the [BYTE_PADDING_IN_DECIMAL] to a sufficiently large number of digits, such that the largest directory
         * size in the records can be representing as decimal number without exceeding this limit.
         */
        const val BYTE_PADDING_IN_DECIMAL = 15
    }
}