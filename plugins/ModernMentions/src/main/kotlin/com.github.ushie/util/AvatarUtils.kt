package com.github.ushie.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import com.discord.models.user.User
import com.discord.utilities.icon.IconUtils
import java.net.URL

// COPIED FROM https://github.com/wingio/plugins/blob/92bd1fb2abe4e2388bd60e9b2a50eb86f97c4f30/FavoriteMessages/src/main/java/xyz/wingio/plugins/favoritemessages/util/AvatarUtils.java
class AvatarUtils(
    private val context: Context,
    private val user: User
) {
    fun toBitmap(): Bitmap? {
        val url = IconUtils.getForUser(user, false, 64) ?: return null
        return fromUrl(context, url)
    }

    companion object {
        @JvmStatic
        fun fromUrl(context: Context, url: String): Bitmap? {
            return try {
                when {
                    url.startsWith("asset://asset/") -> {
                        val assetPath = url.removePrefix("asset://asset/")
                        context.assets.open(assetPath).use { input ->
                            BitmapFactory.decodeStream(input)
                        }
                    }

                    url.startsWith("http://") || url.startsWith("https://") -> {
                        URL(url).openStream().use { input ->
                            BitmapFactory.decodeStream(input)
                        }
                    }

                    else -> null
                }
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        fun makeCircle(bitmap: Bitmap): Bitmap {
            val size = minOf(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2

            val squared = Bitmap.createBitmap(bitmap, x, y, size, size)
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, size, size)
            val radius = size / 2f

            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(radius, radius, radius, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(squared, rect, rect, paint)

            return output
        }
    }
}
