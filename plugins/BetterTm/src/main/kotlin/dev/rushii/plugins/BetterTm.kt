package dev.rushii.plugins

import android.content.Context
import androidx.annotation.DrawableRes
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.plugins.R
import com.aliucord.settings.delegate
import com.discord.utilities.images.MGImages
import com.facebook.imagepipeline.request.ImageRequest
import b.f.j.p.i0 as LocalResourceFetchProducer

const val RES_URI = "res:///"
const val TRADEMARK_EMOJI_RSC_ID = "2131823865"
const val REGISTERED_TRADEMARK_EMOJI_RSC_ID = "2131824088"
const val COPYRIGHT_EMOJI_RSC_ID = "2131824087"

const val CUSTOM_WHITE_PREFIX = "0"
const val CUSTOM_ALT_PREFIX = "00"

/**
 * `res:///` image urls as parsed in [ImageRequest]
 */
const val FRESCO_RES_TYPE = 6

@Suppress("unused")
@AliucordPlugin
class BetterTm : Plugin() {
    var SettingsAPI.useAlt by settings.delegate(true)

    init {
        settingsTab = SettingsTab(
            BetterTmSettings::class.java,
            SettingsTab.Type.BOTTOM_SHEET,
        ).withArgs(this)
    }

    override fun start(context: Context) {
        patcher.before<LocalResourceFetchProducer>(
            "d",
            ImageRequest::class.java,
        ) { (frame, request: ImageRequest) ->
            if (request.d != FRESCO_RES_TYPE) return@before

            val newResourceId = when (request.c.toString()) {
                "${RES_URI}${CUSTOM_WHITE_PREFIX}${TRADEMARK_EMOJI_RSC_ID}" ->
                    R.drawable.ic_trademark_white

                "${RES_URI}${CUSTOM_ALT_PREFIX}${TRADEMARK_EMOJI_RSC_ID}" ->
                    R.drawable.ic_trademark_alt

                "${RES_URI}${CUSTOM_WHITE_PREFIX}${REGISTERED_TRADEMARK_EMOJI_RSC_ID}" ->
                    R.drawable.ic_registered_trademark_white

                "${RES_URI}${CUSTOM_ALT_PREFIX}${REGISTERED_TRADEMARK_EMOJI_RSC_ID}" ->
                    R.drawable.ic_registered_trademark_alt

                "${RES_URI}${CUSTOM_WHITE_PREFIX}${COPYRIGHT_EMOJI_RSC_ID}" ->
                    R.drawable.ic_copyright_white

                "${RES_URI}${CUSTOM_ALT_PREFIX}${COPYRIGHT_EMOJI_RSC_ID}" ->
                    R.drawable.ic_copyright_alt

                else -> return@before
            }

            val size = resources!!.openRawResourceFd(newResourceId).use { it.length }
            val stream = resources!!.openRawResource(newResourceId)

            frame.result = this.c(stream, size.toInt())
        }

        // Rewrite target uri since resources are cached by fresco for some reason
        patcher.before<MGImages?>(
            "getImageRequest",
            String::class.java,
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) {
            it.args[0] = when (it.args[0]) {
                "${RES_URI}${TRADEMARK_EMOJI_RSC_ID}" ->
                    "${RES_URI}${customUriPrefix()}${TRADEMARK_EMOJI_RSC_ID}"

                "${RES_URI}${REGISTERED_TRADEMARK_EMOJI_RSC_ID}" ->
                    "${RES_URI}${customUriPrefix()}${REGISTERED_TRADEMARK_EMOJI_RSC_ID}"

                "${RES_URI}${COPYRIGHT_EMOJI_RSC_ID}" ->
                    "${RES_URI}${customUriPrefix()}${COPYRIGHT_EMOJI_RSC_ID}"

                else -> return@before
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun customUriPrefix() = when (settings.useAlt) {
        true -> CUSTOM_ALT_PREFIX
        false -> CUSTOM_WHITE_PREFIX
    }

    @DrawableRes
    private fun switchAlt(
        @DrawableRes whiteId: Int,
        @DrawableRes altId: Int,
    ): Int {
        return if (settings.useAlt) {
            altId
        } else {
            whiteId
        }
    }
}
