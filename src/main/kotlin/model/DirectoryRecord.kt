package model

import Logging
import logger
import worker.DigestCalculator
import java.util.*

/**
 * Represents a directory in the file system.
 * A [DirectoryRecord] allows for other [RecordAbstract] to be contained in it.
 * The containment is indicated by
 * - the [directories] list for contained [DirectoryRecord].
 * - the [files] list for contained [FileRecord].
 *
 */
class DirectoryRecord(
    id: UUID = UUID.randomUUID(),
    name: String,
    path: String,
    ext: String? = null,
    override var size: Long = 0,
    override var hash: ByteArray = ByteArray(32),
) : RecordAbstract(id, name, path, ext), Logging {
    private val log = logger()

    /**
     * The [DirectoryRecord] representing the directories contained in this directory on the file system.
     */
    val directories: MutableList<DirectoryRecord> = mutableListOf()

    fun add(record: DirectoryRecord): DirectoryRecord {
        directories += record
        return this
    }

    /**
     * The [FileRecord] representing the files contained in this directory on the file system.
     */
    val files: MutableList<FileRecord> = mutableListOf()

    fun add(record: FileRecord): DirectoryRecord {
        files += record
        return this
    }

    /**
     * Recalculate the [size] and the [hash] of this [DirectoryRecord] recursively going down the directory tree.
     * Uses [DigestCalculator] for the hash calculations.
     */
    fun update() {
        directories.forEach { it.update() }

        size = files.sumOf { it.size } + directories.sumOf { it.size }

        hash = DigestCalculator.hashList(
            list = (files.map { it.hash } + directories.map { it.hash })
        )

        log.atInfo().setMessage("Updated DirectoryRecord").addKeyValue("path", path).log()
    }

    fun flatDirectories(): List<DirectoryRecord> {
        directories.ifEmpty {
            return listOf(this)
        }

        return listOf(this) + directories.flatMap { it.flatDirectories() }
    }

    fun flatFiles(): List<FileRecord> {
        directories.ifEmpty {
            return this.files.toList()
        }

        return this.files.toList() + directories.flatMap { it.flatFiles() }
    }
}