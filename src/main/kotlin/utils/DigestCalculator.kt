package utils

import Logging
import logger
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.pathString

@OptIn(ExperimentalStdlibApi::class)
object DigestCalculator : Logging {
    private val log = logger()

    /**
     * The algorithm to use for the digest calculation.
     */
    private const val ALGORITHM = "SHA-256"

    /**
     * Input data is read and processed in chunks.
     * This is the size of a chunk in bytes.
     */
    private const val CHUNK_SIZE = 8192

    /**
     * Calculates the [ALGORITHM] hash of the given [file] reading the file into a [ByteArray] buffer of [CHUNK_SIZE]
     * length to avoid RAM overloading.
     *
     * @param file the path to the file including the file name and extension
     *
     * @return the calculated hash value of [file]
     */
    fun hash(file: Path): ByteArray {
        val digest = MessageDigest.getInstance(ALGORITHM)
        file.inputStream().use {
            val buffer = ByteArray(CHUNK_SIZE)

            var bytesRead = it.read(buffer)
            while (bytesRead != -1) {
                // until no bytes left to read
                digest.update(buffer, 0, bytesRead)
                // update hash using exactly the bytes we read from the file
                bytesRead = it.read(buffer)
                // read next bytes
            }
        }

        return digest.digest().also {
            log.atDebug().setMessage("Calculated MessageDigest of File.")
                .addKeyValue("algorithm", ALGORITHM)
                .addKeyValue("result", it.toHexString(HexFormat.UpperCase))
                .addKeyValue("file.name", file.name)
                .addKeyValue("file.pathString", file.pathString)
                .log()
        }
    }

    /**
     * Calculates the [ALGORITHM] hash of the given [list] feeding each entry of the list to the [MessageDigest] in the
     * order given by [list]. Note: The order of the [list] matters for the resulting hash.
     * This method is not permutation invariant.
     *
     * @param list the list of [ByteArray] to feed into the hashing algorithm
     *
     * @return the calculated hash value of [list]
     */
    fun hash(list: List<ByteArray>): ByteArray {
        val digest = MessageDigest.getInstance(ALGORITHM)

        list.forEach { digest.update(it) }

        return digest.digest().also {
            log.atDebug().setMessage("Calculated MessageDigest of List.")
                .addKeyValue("algorithm", ALGORITHM)
                .addKeyValue("result", it.toHexString(HexFormat.UpperCase))
                .addKeyValue("list.size", list.size)
                .log()
        }
    }
}