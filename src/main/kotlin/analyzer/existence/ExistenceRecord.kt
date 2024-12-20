package analyzer.existence

import model.IRecord

/**
 * Represents a [T] record that declares all paths of [IRecord] that can be unionized (without respecting the file system structure) to be this record.
 */
@Suppress("unused")
class ExistenceRecord<T : IRecord>(
    private val record: T,
    val exists: Boolean,
    val coverage: List<String>
) : IRecord by record