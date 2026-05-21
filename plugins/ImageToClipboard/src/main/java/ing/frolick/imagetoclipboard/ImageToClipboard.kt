package ing.frolick.imagetoclipboard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.Utils.appContext
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.app.AppFragment
import com.discord.databinding.WidgetMediaBinding
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.media.WidgetMedia

import com.google.android.material.appbar.AppBarLayout
import com.lytefast.flexinput.R
import java.io.File
import java.io.InputStream
import java.net.URL

@SuppressWarnings("unused")
@AliucordPlugin
class ImageToClipboard : Plugin() {
    // https://stackoverflow.com/questions/14473180/regex-to-get-a-filename-from-a-url#comment115606945_26253039
    var urlRegex = Regex("[^/\\\\&?#]+\\.\\w{3,4}(?=([?&#].*$|$))")

    @SuppressLint("RestrictedApi")
    override fun start(context: Context) {
        patcher.patch(
            WidgetMedia::class.java, "onViewBoundOrOnResume", arrayOf(),
            Hook {
                val widgetMedia = it.thisObject as WidgetMedia
                val binding = WidgetMedia.`access$getBinding$p`(widgetMedia)
                val field = WidgetMediaBinding::class.java.getDeclaredField("b").apply { isAccessible = true }
                val appBarLayout = field.get(binding) as AppBarLayout
                val menuMediaGroup = appBarLayout.findViewById<TextView>("menu_media_download").parent as ViewGroup

                val imageUrl = (widgetMedia as AppFragment).mostRecentIntent
                    .getStringExtra("INTENT_MEDIA_URL") ?: return@Hook
                val fileName = urlRegex.find(imageUrl, 0)?.value
                val fileExt = fileName?.split('.')?.last()
                var mimeType = MimeType.OTHER
                Utils.threadPool.submit {
                    mimeType = MimeTypeGetter.get(imageUrl)
                }.get()
                //logger.info("MIME TYPE: ${mimeType.type}")

                //Logger().info("CHILDREN COUNT: ${actionBar.childCount}")
                //val menuMediaGroup = actionBar.getChildAt(0) as ViewGroup

                val ctx = menuMediaGroup.context
                val copyBtn = TextView(ctx, null, 0, R.i.UiKit_ImageButton).apply {
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat
                            .getDrawable(ctx, R.e.ic_copy_24dp)!!
                            .mutate()
                            .apply {
                                setTint(ColorCompat.getThemedColor(ctx, R.b.colorTextNormal))
                            },
                        null,
                        null,
                        null,
                    )
                    setOnClickListener {
                        Utils.showToast("copying...", false)

                        Utils.threadPool.execute {
                            try {
                                val stream = URL(imageUrl).openStream()

                                // context.filesDir is /data/data/com.aliucord/files, we are saving the image there
                                // in theory it'd be better to use context.cacheDir, but discord doesn't expose that
                                // in file_paths.xml, so it's not usable
                                val file = File(context.filesDir, "clipboard_tmp.${mimeType.ext}")
                                file.outputStream().use { out -> stream.copyTo(out) }
                                val uri = FileProvider.getUriForFile(
                                    appContext,
                                    "${context.packageName}.file-provider",
                                    file
                                )
                                val clip = ClipData(
                                    ClipDescription("image", arrayOf(mimeType.type)),
                                    ClipData.Item(uri)
                                )
                                (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                    .setPrimaryClip(clip)

                                Utils.mainThread.post { Utils.showToast("copied the image!", false) }
                            } catch (e: Exception) {
                                logger.error("itc error: ${e.message}", e)
                                Utils.mainThread.post { Utils.showToast("failed to copy >.<", false) }
                            }
                        }
                    }
                }
                if (appBarLayout.findViewWithTag<View>("copy_btn") == null) {
                    // non image begone!!!
                    if (fileExt == "png" ||
                        fileExt == "jpg" ||
                        fileExt == "jpeg" ||
                        fileExt == "webp" ||
                        fileExt == "gif" ||
                        fileExt == "apng") {
                            copyBtn.tag = "copy_btn"
                            menuMediaGroup.addView(copyBtn)
                    }
                }
            },
        )
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
