import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Define the [Logging] interface for classes that may want to use a logger.
 * This interface is used as base for the kotlin extension function that provides simple access to an [Logger] from
 * [org.slf4j] using the [LoggerFactory].
 *
 * Create a private property inside your class that will provide the [Logger] instance returned by the [logger] method.
 * This property can then be used to log inside your class, where the logger name is automatically assigned.
 *
 */
interface Logging

/**
 * This extension function simplifies the logger factory call.
 * The class of [T] is automatically injected into the logger.
 *
 * We use `inline` and `reified` here to avoid reflection at runtime.
 */
inline fun <reified T : Logging> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)