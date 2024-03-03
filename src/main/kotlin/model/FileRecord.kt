package model

import java.util.*

/**
 * Represents a file in the file system of the scanned directory.
 * Does not allow for other [RecordAbstract] to be contained in it.
 * This class is always an end node in the directory tree.
 */
class FileRecord(
    id: UUID = UUID.randomUUID(),
    name: String,
    path: String,
    override var size: Long,
    override var hash: ByteArray
) : RecordAbstract(id, name, path)