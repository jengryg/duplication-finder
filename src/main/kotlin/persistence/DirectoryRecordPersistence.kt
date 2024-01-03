package persistence

import Logging
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import logger
import model.DirectoryRecord
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.absolutePathString

object DirectoryRecordPersistence : Logging {
    private val log = logger()

    fun load(jsonFile: String): DirectoryRecord {
        Path.of(jsonFile).also { path ->
            assert(Files.exists(path))

            return Files.readString(path).let { json ->
                jacksonObjectMapper().readValue<DirectoryRecord>(json)
            }.also {
                log.atInfo()
                    .setMessage("Loaded DirectoryRecord from json.")
                    .addKeyValue("source", path.absolutePathString())
                    .log()
            }
        }
    }

    fun save(jsonFile: String, record: DirectoryRecord) {
        Path.of(jsonFile).also { path ->

            Files.writeString(path, jacksonObjectMapper().writeValueAsString(record), StandardOpenOption.CREATE_NEW)

            log.atInfo()
                .setMessage("Saved DirectoryRecord to json.")
                .addKeyValue("destination", path.absolutePathString())
                .log()
        }
    }
}