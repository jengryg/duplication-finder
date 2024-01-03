package model

import java.util.*

/**
 * Represents a file in the file system.
 * A [FileRecord] does not allow for other [RecordAbstract] to be contained in it.
 * It is an end node in the directory tree.
 */
class FileRecord(
    id: UUID = UUID.randomUUID(),
    name: String,
    path: String,
    ext: String? = null,
    override var size: Long,
    override var hash: ByteArray
) : RecordAbstract(id, name, path, ext)