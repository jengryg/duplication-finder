package worker

import Logging
import logger
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.name
import kotlin.io.path.pathString

object DigestCalculator : Logging {
    private val log = logger()

    /**
     * The [MessageDigest] algorithm to use.
     */
    private const val algorithm = "SHA-256"

    /**
     * Input data is read at [chunkSize] per iteration.
     */
    private const val chunkSize = 8192

    /**
     * Calculates the [algorithm] hash of the given [file] using an [InputStream] to read it in chunks using a
     * [ByteArray] of [chunkSize] length as buffer.
     *
     * @return the [algorithm] hash
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun hashFile(file: Path): ByteArray {
        val digest = MessageDigest.getInstance(algorithm)
        Files.newInputStream(file).use { inputStream: InputStream ->
            val buffer = ByteArray(chunkSize)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                // until no bytes left to read
                digest.update(buffer, 0, bytesRead)
                // update hash
                bytesRead = inputStream.read(buffer)
                // read next bytes
            }
        }

        return digest.digest().also {
            log.atDebug().setMessage("Calculated MessageDigest of File")
                .addKeyValue("algorithm", algorithm)
                .addKeyValue("result", it.toHexString(HexFormat.UpperCase))
                .addKeyValue("file.name", file.name)
                .addKeyValue("file.pathString", file.pathString)
                .log()
        }
    }

    /**
     * Calculates the [algorithm] hash of the given [list] feeding each entry of the list to the [MessageDigest] in
     * the order given by [list]. Note: The order of the [list] matters for the resulting hash. This method is not
     * permutation invariant.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun hashList(list: List<ByteArray>): ByteArray {
        val digest = MessageDigest.getInstance(algorithm)

        list.forEach {
            digest.update(it)
        }

        return digest.digest().also {
            log.atDebug().setMessage("Calculated MessageDigest of List")
                .addKeyValue("algorithm", algorithm)
                .addKeyValue("result", it.toHexString(HexFormat.UpperCase))
                .addKeyValue("list.size", list.size)
                .log()
        }
    }
}