package fund.cyber.markets.common

import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readAsString(charset: Charset = Charsets.UTF_8): String {
    return this.reader(charset).use { it.readText() }
}
