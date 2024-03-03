package model

import Logging
import logger
import utils.DigestCalculator
import java.util.*

/**
 * Represents a directory in the file system of the scanned directory.
 * Does allow for other [RecordAbstract] to be contained in it:
 * - Files contained in this directory are represented with [FileRecord] in [files].
 * - Directories contained in this directory are represented with [DirectoryRecord] in [directories].
 */
class DirectoryRecord(
    id: UUID = UUID.randomUUID(),
    name: String,
    path: String,
    override var size: Long = 0,
    override var hash: ByteArray = ByteArray(32),
) : RecordAbstract(id, name, path), Logging {
    private val log = logger()

    /**
     * The list of [DirectoryRecord] representing the subdirectories contained in this directory on the file system.
     */
    val directories: MutableList<DirectoryRecord> = mutableListOf()

    fun add(record: DirectoryRecord) {
        directories += record
    }

    /**
     * The list of [FileRecord] representing the files contained in this directory on the file system.
     */
    val files: MutableList<FileRecord> = mutableListOf()

    fun add(record: FileRecord) {
        files += record
    }

    /**
     * Recalculates the [size] and the [hash] of this [DirectoryRecord].
     * This method uses recursion to work through the directory tree that has this directory as root.
     */
    fun update() {
        directories.forEach { it.update() }

        size = files.sumOf { it.size } + directories.sumOf { it.size }

        hash = DigestCalculator.hash(
            list = (files.map { it.hash } + directories.map { it.hash })
        )

        log.atInfo()
            .setMessage("Updated DirectoryRecord.")
            .addKeyValue("path", path)
            .log()
    }

    /**
     * Transform the tree representation of all [DirectoryRecord] to a list containing all of them.
     *
     * @return list of all [DirectoryRecord] in this directory and all of its subdirectories.
     */
    fun flatDirectories(): List<DirectoryRecord> {
        directories.ifEmpty {
            return listOf(this)
        }

        return listOf(this) + directories.flatMap { it.flatDirectories() }
    }

    /**
     * Transform the tree representation of all [FileRecord] to a list containing all of them.
     *
     * @return list of all [FileRecord] in this directory and all of its subdirectories.
     */
    fun flatFiles(): List<FileRecord> {
        directories.ifEmpty {
            return files.toList()
        }

        return files.toList() + directories.flatMap { it.flatFiles() }
    }
}