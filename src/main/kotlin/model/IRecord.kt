package model

import java.util.*

interface IRecord {
    /**
     * A unique identifier for this record.
     */
    val id: UUID

    /**
     * The name of this record in the filesystem.
     */
    val name: String

    /**
     * The path of this record in the filesystem.
     */
    val path: String

    /**
     * The size in bytes of the represented element in the file system.
     */
    var size: Long

    /**
     * The checksum in bytes of the represented element in the file system.
     */
    var hash: ByteArray

    /**
     * Two [IRecord] are considered duplicates of each other, if this [groupId] is the same.
     */
    val groupId: String
}

/**
 * Extension function to filter out all [IRecord] that are not at least 1 byte in size.
 */
fun <T : IRecord> List<T>.filterNoZeroBytes(): List<T> {
    return this.filter { it.size > 0L }
}

/**
 * Extension function to group all [IRecord] into a map having the [IRecord.groupId] as key and a list as value.
 * The list contains all [IRecord] that have equal groupIds, i.e. are considered to be duplicates of each other.
 */
fun <T : IRecord> List<T>.determineGroups(): Map<String, List<T>> {
    return this.groupBy { it.groupId }
}