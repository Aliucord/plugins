package ing.frolick.imagetoclipboard

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.Arrays.asList
import com.aliucord.Logger
import java.net.HttpURLConnection

class MimeTypeGetter {
    companion object {
        private fun fromMagicNumber(url: String): MimeType {
            val stream = URL(url).openStream()
            val header = ByteArray(12).also { buf ->
                var pos = 0
                while (pos < buf.size) {
                    val n = stream.read(buf, pos, buf.size - pos)
                    if (n == -1) break
                    pos += n
                }
            }
            stream.close()
            fun at(vararg bytes: Int, offset: Int = 0) =
                bytes.indices.all { header[offset + it].toInt() and 0xFF == bytes[it] }

            // TODO: FIX APNG DETECTION AND MAKE IT OPTIMAL, THIS ALGO IS REALLY SLOW AND BROKEN
            // only real way to detect APNG is to look for the animation control chunk (acTL)
            // that comes later in the file, since the format has the same file magic as PNG
            /*if (at(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
                val buf = ArrayDeque<Int>()
                // read first 4 bytes for sliding window
                while (buf.size <= 4) buf.addLast(stream.read().also { if (it == -1) return MimeType.PNG })
                Logger("piglin").info("CHUNK: ${ArrayDeque(listOf(0x49, 0x44, 0x41, 0x54)).joinToString(" ") { "%02x".format(it.toByte()) }}")

                while (buf.intersect(listOf(0x61, 0x63, 0x54, 0x4C)).isNotEmpty()) {
                    // acTL always comes before the IDAT chunk. IDAT chunk before acTL == PNG
                    if (buf.intersect(listOf(0x49, 0x44, 0x41, 0x54)).isEmpty()) return MimeType.PNG

                    buf.removeFirst()
                    val n = stream.read()
                    //Logger("piglin").info("CHUNK: ${buf.joinToString(" ") { "%02x".format(it.toByte()) }}")
                    if (n == -1) return MimeType.PNG
                    buf.addLast(n)
                }
                return MimeType.APNG
            }*/

            return when {
                at(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> MimeType.PNG
                at(0xFF, 0xD8, 0xFF, 0xDB) -> MimeType.JPEG
                at(0xFF, 0xD8, 0xFF, 0xE0) -> MimeType.JPEG
                at(0xFF, 0xD8, 0xFF, 0xEE) -> MimeType.JPEG
                at(0xFF, 0xD8, 0xFF, 0xE1) -> MimeType.JPEG
                at(0x47, 0x49, 0x46, 0x38, 0x37, 0x61) -> MimeType.GIF
                at(0x47, 0x49, 0x46, 0x38, 0x39, 0x61) -> MimeType.GIF
                at(0x42, 0x4D) -> MimeType.BMP
                // webp: RIFF????WEBP
                at(0x52, 0x49, 0x46, 0x46) &&
                    at(0x57, 0x45, 0x42, 0x50, offset = 8) -> MimeType.WEBP
                else -> MimeType.OTHER
            }
        }

        private fun fromHeaders(url: String): MimeType? {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                this.instanceFollowRedirects = true
                this.requestMethod = "HEAD"
            }
            connection.connect()
            connection.headerFields["Content-Type"]?.let {
                return MimeType.entries.firstOrNull { e -> e.type == it.first() } ?: MimeType.OTHER
            }
            return null
        }

        fun get(url: String): MimeType {
            return fromHeaders(url) ?: fromMagicNumber(url)
        }
    }
}

enum class MimeType(val type: String, val ext: String) {
    PNG("image/png", "png"),
    APNG("image/apng", "png"),
    JPEG("image/jpeg", "jpg"),
    GIF("image/gif", "gif"),
    BMP("image/x-ms-bmp", "bmp"),
    WEBP("image/webp", "webp"),
    OTHER("", "bin")
}
