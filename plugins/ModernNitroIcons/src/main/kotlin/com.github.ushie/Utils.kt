package com.github.ushie

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.aliucord.BuildConfig
import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.Utils

// https://github.com/Aliucord/aliucord/blob/34d68dd1fc3364e1da135d86a757a14bd6fe3105/Aliucord/src/main/java/com/aliucord/coreplugins/badges/Utils.kt
private val imageCache = HashMap<String, Bitmap>()

/**
 * Set a remote image to be displayed in this view.
 * This will be cached in memory for the lifecycle of the application.
 * As such, this should only be used for long-living and often used images.
 */
internal fun ImageView.setCacheableImage(url: String) {
    val cachedImage = imageCache[url]

    if (cachedImage != null) {
        setImageBitmap(cachedImage)
    } else {
        Utils.threadPool.execute {
            try {
                val image = Http.Request(url)
                    .setHeader("User-Agent", "Aliucord/${BuildConfig.VERSION}")
                    .execute()
                    .stream()
                    .let(BitmapFactory::decodeStream)

                Utils.mainThread.post {
                    imageCache[url] = image
                    setImageBitmap(image)
                }
            } catch (e: Exception) {
                Logger("ImageCache").warn("Failed to retrieve image $url", e)
            }
        }
    }
}