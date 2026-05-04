package moe.lava.awoocord.dns

import android.graphics.Typeface
import android.os.Build
import androidx.core.graphics.TypefaceCompat
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.utils.RxUtils.subscribe
import rx.subjects.BehaviorSubject
import java.io.File

internal object FontHandler {
    private val cachedTypefaces = mutableMapOf<FontStyle, BehaviorSubject<Typeface>>()

    fun fetch(style: FontStyle, onValue: (Typeface) -> Unit) {
        val subject = cachedTypefaces.getOrPut(style) {
            logger.info("finding $style")
            val subject = BehaviorSubject.k0<Typeface>()
            Utils.threadPool.execute {
                val path = File(Utils.appActivity.cacheDir, "fonts/${style.name}.ttf")
                if (!path.exists()) {
                    path.parentFile?.mkdirs()
                    logger.info("fetching $style")
                    Http.Request.newDiscordRequest(style.url).execute().saveToFile(path)
                    logger.info("fetched $style to $path")
                }
                logger.info("found $style")
                val typeface = Typeface.createFromFile(path)
                // If we get androidx core 1.9.0 we can use TypefaceCompat.create with a weight
                if (style.isVariable && Build.VERSION.SDK_INT >= 28) {
                    subject.onNext(Typeface.create(typeface, 500, false))
                } else {
                    subject.onNext(TypefaceCompat.create(Utils.appContext, typeface, Typeface.NORMAL))
                }
            }
            subject
        }

        subject.z() // .first()
            .subscribe(onValue)
    }
}
