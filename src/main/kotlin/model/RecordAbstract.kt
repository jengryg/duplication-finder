package model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.UUID

/**
 * An abstract class to represent a [java.nio.file.Path] element.
 */
abstract class RecordAbstract(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val path: String,
    val ext: String? = null
) {
    /**
     * The size in bytes of the represented element in the file system.
     */
    abstract var size: Long

    /**
     * The [worker.DigestCalculator] checksum in bytes of the represented element in the file system.
     */
    abstract var hash: ByteArray

    @get:JsonIgnore
    @OptIn(ExperimentalStdlibApi::class)
    val hashAsHex: String get() = hash.toHexString(HexFormat.UpperCase)

    override fun toString(): String {
        return "${this::class.simpleName}: id=$id, hash=$hashAsHex, path=$path, size=$size, ext=$ext"
    }
}