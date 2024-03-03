package analyzer.duplicates

import model.IRecord

/**
 * Represents a [T] record and all paths of [T] records that are considered to be duplicates of this record.
 */
class DuplicateRecord<T : IRecord>(
    private val record: T,
    /**
     * All paths of records that are considered to be duplicates of this one.
     */
    val duplicates: List<String>,
) : IRecord by record