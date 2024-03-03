package model

import SIZE_IN_BYTES_FIXED_LENGTH_IN_DECIMAL
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

/**
 * An abstract class to represent an element in the directory structure.
 */
abstract class RecordAbstract(
    override val id: UUID = UUID.randomUUID(),
    override val name: String,
    override val path: String,
) : IRecord {

    /**
     * The calculated [hash] value represented in HEX.
     */
    @OptIn(ExperimentalStdlibApi::class)
    val hashHex: String get() = hash.toHexString()

    /**
     * Use the calculated [hash] value represented in HEX followed by a `-` (dash) and the [size] in bytes padded from
     * the left with ` ` (space) until the fixed length of [SIZE_IN_BYTES_FIXED_LENGTH_IN_DECIMAL] is reached.
     *
     * `<HASH in HEX>-<SIZE IN BYTES PADDED TO FIXED LENGTH>`
     */
    @get:JsonIgnore
    override val groupId: String
        get() {
            return "${hashHex}-${
                size.toString().padStart(SIZE_IN_BYTES_FIXED_LENGTH_IN_DECIMAL)
            }"
        }

    override fun toString(): String {
        return "${this::class.simpleName}: id=$id, path=$path, size=$size"
    }
}