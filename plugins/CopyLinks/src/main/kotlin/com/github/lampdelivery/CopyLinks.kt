package com.github.lampdelivery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.databinding.WidgetChannelsListItemActionsBinding
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions
import com.lytefast.flexinput.R
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.isDM
import com.discord.utilities.color.ColorCompat
import com.aliucord.Constants
import androidx.core.content.res.ResourcesCompat

@AliucordPlugin(requiresRestart = false)
class CopyLinks : Plugin() {
    override fun start(context: Context) {
        patcher.after<WidgetChannelsListItemChannelActions>("configureUI", WidgetChannelsListItemChannelActions.Model::class.java) { param ->
            val model = param.args[0] as? WidgetChannelsListItemChannelActions.Model ?: return@after
            val channel = model.channel
            if (channel.isDM()) return@after
            val guildId = channel.guildId
            val channelId = channel.id
            val link = "https://discord.com/channels/$guildId/$channelId"

            val getBindingMethod = WidgetChannelsListItemChannelActions::class.java.getDeclaredMethod("getBinding").apply { isAccessible = true }
            val binding = getBindingMethod.invoke(param.thisObject) as WidgetChannelsListItemActionsBinding
            val root = binding.root as android.view.ViewGroup
            val ctx = root.context
            val linearLayout = root.getChildAt(0) as? LinearLayout ?: return@after

            if ((0 until linearLayout.childCount).any {
                    val v = linearLayout.getChildAt(it)
                    v is TextView && v.text == "Copy Link"
                }) return@after

            var markAsReadIdx = -1
            for (i in 0 until linearLayout.childCount) {
                val v = linearLayout.getChildAt(i)
                if (v is TextView && v.text.toString().contains("Mark As Read", ignoreCase = true)) {
                    markAsReadIdx = i
                    break
                }
            }
            val insertIdx = if (markAsReadIdx >= 0) markAsReadIdx else linearLayout.childCount

            val icon = ContextCompat.getDrawable(ctx, R.e.ic_link_white_24dp)?.mutate()
            icon?.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))

            val mediumTypeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)

            val copyLinkView = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                text = "Copy Link"
                typeface = mediumTypeface
                setCompoundDrawablesWithIntrinsicBounds(
                    icon,
                    null, null, null
                )
                setOnClickListener { v ->
                    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Channel Link", link))
                    dismiss()
                }
            }
            linearLayout.addView(copyLinkView, insertIdx)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
