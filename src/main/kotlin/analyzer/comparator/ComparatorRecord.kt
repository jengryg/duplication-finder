package analyzer.comparator

import model.IRecord

/**
 * Represents a [T] record and all paths of [T] records that are considered to be matches of this record.
 */
class ComparatorRecord<T : IRecord>(
    private val record: T,
    /**
     * All paths of records that are considered to be matches of this one.
     */
    val matches: List<String>
) : IRecord by record