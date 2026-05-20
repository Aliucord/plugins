package com.github.ushie

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.NestedScrollView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.utils.lazyMethod
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.nsfw
import com.discord.api.message.attachment.MessageAttachment
import com.discord.api.message.embed.MessageEmbed
import com.discord.databinding.WidgetGuildContextMenuBinding
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemAttachment
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed
import com.discord.widgets.chat.list.entries.AttachmentEntry
import com.discord.widgets.chat.list.entries.EmbedEntry
import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu
import com.google.gson.reflect.TypeToken
import com.lytefast.flexinput.R
import java.lang.reflect.Type
import java.util.WeakHashMap
import b.a.a.d.a as UserActionsDialog

@AliucordPlugin
class BetterSpoiler : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private val getServerBindingMethod by lazyMethod<WidgetGuildContextMenu>("getBinding")

    private val attachmentMap = WeakHashMap<MessageAttachment, AttachmentEntry>()
    private val embedMap = WeakHashMap<MessageEmbed, EmbedEntry>()
    private val nsfwChannelsCache = HashMap<Long, Boolean>()

    private var spoilerInChannels: HashSet<Long> = HashSet()
    private var spoilerInGuilds: HashSet<Long> = HashSet()
    private var spoilerInUsers: HashSet<Long> = HashSet()
    private var spoilerNsfw: Boolean = false

    val setType: Type = object : TypeToken<HashSet<Long>>() {}.type

    override fun start(context: Context) {
        spoilerInChannels = settings.getObject("spoiler_in_channels", HashSet(), setType)
        spoilerInGuilds = settings.getObject("spoiler_in_guilds", HashSet(), setType)
        spoilerInUsers = settings.getObject("spoiler_in_users", HashSet(), setType)
        spoilerNsfw = settings.getBool("spoiler_nsfw_channels", false)

        patcher.before<WidgetChatListAdapterItemAttachment.Model>(
            AttachmentEntry::class.java,
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Long::class.javaPrimitiveType!!
        ) {
            val attachmentEntry = it.args[0] as? AttachmentEntry ?: return@before
            attachmentMap[attachmentEntry.attachment] = attachmentEntry
        }

        patcher.after<MessageAttachment>("h") {
            val entry = attachmentMap[this] ?: return@after
            val message = entry.message

            it.result = when {
                message.author.id in spoilerInUsers -> true
                entry.guildId in spoilerInGuilds -> true
                message.channelId in spoilerInChannels -> true
                spoilerNsfw -> nsfwChannelsCache.getOrPut(message.channelId) {
                    StoreStream.getChannels().getChannel(message.channelId)?.nsfw ?: false
                }

                else -> return@after
            }
        }

        patcher.before<WidgetChatListAdapterItemEmbed.Model>(
            EmbedEntry::class.java,
            Collection::class.java,
            List::class.java,
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Long::class.javaPrimitiveType!!
        ) {
            val embedEntry = it.args[0] as? EmbedEntry ?: return@before
            embedMap[embedEntry.embed] = embedEntry
        }

        patcher.after<WidgetChatListAdapterItemEmbed.Model>("isSpoilerEmbed") {
            val entry = embedMap[this.embedEntry.embed] ?: return@after
            val message = entry.message

            it.result = when {
                message.author.id in spoilerInUsers -> true
                entry.guildId in spoilerInGuilds -> true
                message.channelId in spoilerInChannels -> true
                spoilerNsfw -> nsfwChannelsCache.getOrPut(message.channelId) {
                    StoreStream.getChannels().getChannel(message.channelId)?.nsfw ?: false
                }
                else -> return@after
            }
        }

        patcher.after<WidgetChannelsListItemChannelActions>(
            "configureUI",
            WidgetChannelsListItemChannelActions.Model::class.java
        ) { param ->
            val model = param.args[0] as? WidgetChannelsListItemChannelActions.Model ?: return@after
            val layout = (view as? NestedScrollView)?.getChildAt(0) as? LinearLayout ?: return@after

            createContextOption(
                layout = layout,
                set = spoilerInChannels,
                targetId = model.channel.id,
                "spoiler_in_channels",
                style = R.i.UiKit_Settings_Item_Icon,
                onUpdate = {
                    spoilerInChannels = it
                    dismiss()
                }
            )
        }

        patcher.after<WidgetGuildContextMenu>(
            "configureUI",
            GuildContextMenuViewModel.ViewState::class.java
        ) { param ->
            val state = param.args[0] as? GuildContextMenuViewModel.ViewState.Valid ?: return@after
            val binding =
                getServerBindingMethod.invoke(this) as? WidgetGuildContextMenuBinding ?: return@after
            val layout = binding.e.parent as? LinearLayout ?: return@after
            val guildId = state.guild.id

            createContextOption(layout, spoilerInGuilds, guildId, "spoiler_in_guilds") { spoilerInGuilds = it }
        }

        patcher.after<UserActionsDialog>("onViewBound", View::class.java) { param ->
            val view = param.args[0] as LinearLayout

            val userId = this.argumentsOrDefault
                .getLong("com.discord.intent.extra.EXTRA_USER_ID", 0L)
                .takeIf { it != 0L } ?: return@after

            createContextOption(view, spoilerInUsers, userId, "spoiler_in_users") { spoilerInUsers = it }
        }
    }

    private fun createContextOption(
        layout: LinearLayout,
        set: HashSet<Long>,
        targetId: Long,
        settingsKey: String,
        style: Int = R.i.ContextMenuTextOption,
        onUpdate: (HashSet<Long>) -> Unit,
    ) {
        val isActive = targetId in set

        layout.addView(TextView(layout.context, null, 0, style).apply {
            id = View.generateViewId()
            text = if (isActive) "Disable Always Spoiler" else "Enable Always Spoiler"
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    layout.context,
                    if (isActive) R.e.design_ic_visibility_off else R.e.design_ic_visibility
                )?.tinted(layout.context),
                null, null, null
            )
            setOnClickListener {
                val updated = HashSet(if (isActive) set - targetId else set + targetId)
                settings.setObject(settingsKey, updated)
                onUpdate(updated)
                layout.visibility = View.GONE
            }
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        attachmentMap.clear()
        nsfwChannelsCache.clear()
    }
}

private fun Drawable.tinted(context: Context): Drawable = mutate().apply {
    setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
}
