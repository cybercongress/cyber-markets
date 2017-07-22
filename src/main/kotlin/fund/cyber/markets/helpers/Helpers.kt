package fund.cyber.markets.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Helper function to create logger.
 *
 * @author Ibragimov Ruslan
 */
fun logger(klass: KClass<*>): Logger = LoggerFactory.getLogger(klass.java)
