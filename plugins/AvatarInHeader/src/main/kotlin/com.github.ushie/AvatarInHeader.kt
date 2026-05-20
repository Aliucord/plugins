package com.github.ushie

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.ChannelWrapper.Companion.isDM
import com.aliucord.wrappers.ChannelWrapper.Companion.isGuild
import com.aliucord.wrappers.ChannelWrapper.Companion.recipients
import com.discord.databinding.WidgetHomeBinding
import com.discord.models.user.CoreUser
import com.discord.stores.StoreStream
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.images.MGImages
import com.discord.widgets.home.WidgetHome
import com.discord.widgets.home.WidgetHomeHeaderManager
import com.discord.widgets.home.WidgetHomeModel
import com.facebook.drawee.view.SimpleDraweeView

private enum class AvatarType(val settingKey: String, val defaultValue: Boolean) {
    DM("showDmAvatar", true),
    GROUP("showGroupAvatar", true),
    GUILD("showServerAvatar", false)
}

@Suppress("unused")
@AliucordPlugin
class AvatarInHeader : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val iconId = Utils.getResId("toolbar_icon", "id")
        val viewId = View.generateViewId()

        val size = DimenUtils.dpToPx(24f)

        patcher.after<WidgetHomeHeaderManager>(
            "configure",
            WidgetHome::class.java,
            WidgetHomeModel::class.java,
            WidgetHomeBinding::class.java
        ) { param ->
            val model = param.args[1] as WidgetHomeModel
            val layout = (param.args[0] as WidgetHome).actionBarTitleLayout ?: return@after
            val icon = layout.findViewById<View>(iconId) ?: return@after
            val avatar = layout.findViewWithTag<SimpleDraweeView>("AvatarInHeaderTag")

            val channel = model.channel ?: return@after
            val recipients = channel.recipients.orEmpty()

            val avatarType = when {
                channel.isDM() && recipients.size == 1 -> AvatarType.DM
                channel.isDM() -> AvatarType.GROUP
                channel.isGuild() -> AvatarType.GUILD
                else -> null
            }

            val iconUri = avatarType
                ?.takeIf { settings.getBool(it.settingKey, it.defaultValue) }
                ?.let {
                    when (it) {
                        AvatarType.DM ->
                            IconUtils.getForUser(CoreUser(recipients.first()))

                        AvatarType.GROUP ->
                            IconUtils.getForChannel(channel, 2048)

                        AvatarType.GUILD ->
                            StoreStream.getGuilds().getGuild(channel.guildId)?.let { guild ->
                                IconUtils.getForGuild(guild.id, guild.icon, guild.icon, false)
                            }
                    }
                }

            if (iconUri == null) {
                icon.tag = null
                icon.alpha = 1f
                avatar?.visibility = View.GONE
                return@after
            }

            if (icon.tag == iconUri) return@after
            icon.tag = iconUri
            icon.alpha = 0f

            val avatarView = avatar ?: SimpleDraweeView(layout.context).apply {
                tag = "AvatarInHeaderTag"
                id = viewId

                val params = icon.layoutParams as ConstraintLayout.LayoutParams
                layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                    topToTop = params.topToTop
                    bottomToBottom = params.bottomToBottom
                }

                (icon.parent as ViewGroup).addView(this)
                MGImages.setRoundingParams(this, size / 2f, false, null, null, 0f)
            }

            avatarView.visibility = View.VISIBLE
            avatarView.setImageURI(iconUri)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
