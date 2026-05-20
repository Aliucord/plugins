package com.github.ushie

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.lazyField
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.api.channel.Channel
import com.discord.databinding.WidgetChannelsListItemChannelStageVoiceBinding
import com.discord.databinding.WidgetChannelsListItemChannelVoiceBinding
import com.discord.models.domain.ModelNotificationSettings
import com.discord.stores.StoreStream
import com.discord.stores.StoreUserGuildSettings
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.list.WidgetChannelListModel
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemStageVoiceChannel
import com.discord.widgets.channels.list.items.ChannelListItemVoiceChannel
import com.lytefast.flexinput.R
import rx.Observable

@Suppress("UNCHECKED_CAST", "unused")
@AliucordPlugin
class MuteVoiceAndStageChannels : Plugin() {
    private val voiceNameId = Utils.getResId("channels_item_voice_channel_name", "id")
    private val voiceIconId = Utils.getResId("channels_item_voice_channel_speaker", "id")
    private val stageNameId = Utils.getResId("stage_channel_item_voice_channel_name", "id")
    private val stageIconId = Utils.getResId("stage_channel_item_stage_channel_icon", "id")

    private val guildSettingsField by lazyField<StoreUserGuildSettings>("guildSettings")
    private val hideMutedGuildsField by lazyField<StoreUserGuildSettings>("guildsToHideMutedChannelsIn")

    private val voiceBindingField by lazyField<WidgetChannelsListAdapter.ItemChannelVoice>("binding")
    private val stageBindingField by lazyField<WidgetChannelsListAdapter.ItemChannelStageVoice>("binding")

    private val userGuildSettings
        get() = StoreStream.getUserGuildSettings()

    private val mutedOverrides = mutableMapOf<Long, Boolean>()

    override fun start(context: Context) {
        patcher.after<WidgetChannelListModel.Companion>("get") { param ->
            val observable = param.result as? Observable<WidgetChannelListModel> ?: return@after

            param.result = observable.G { model ->
                model.copy(
                    model.selectedGuild,
                    model.items.filterNot { item ->
                        when (item) {
                            is ChannelListItemVoiceChannel -> item.channel.shouldHide()
                            is ChannelListItemStageVoiceChannel -> item.channel.shouldHide()
                            else -> false
                        }
                    },
                    model.isGuildSelected,
                    model.showPremiumGuildHint,
                    model.showEmptyState,
                    model.guildScheduledEvents
                )
            }
        }

        patcher.after<WidgetChannelsListAdapter.ItemChannelVoice>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java
        ) { param ->
            val data = param.args[1] as? ChannelListItemVoiceChannel ?: return@after
            val binding = voiceBindingField.get(param.thisObject)
                as WidgetChannelsListItemChannelVoiceBinding
            binding.a.setOnLongClickListener {
                data.channel.toggleMuted(it)
                true
            }
            if (!data.channel.isMuted() || data.voiceSelected) return@after
            val itemView = (param.thisObject as WidgetChannelsListAdapter.ItemChannelVoice).itemView
            tintMutedVoiceItem(
                itemView.context,
                (param.thisObject as WidgetChannelsListAdapter.ItemChannelVoice).itemView,
                voiceNameId,
                voiceIconId
            )
        }

        patcher.after<WidgetChannelsListAdapter.ItemChannelStageVoice>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java
        ) { param ->
            val data = param.args[1] as? ChannelListItemStageVoiceChannel ?: return@after
            val binding = stageBindingField.get(param.thisObject)
                as WidgetChannelsListItemChannelStageVoiceBinding
            binding.a.setOnLongClickListener {
                data.channel.toggleMuted(it)
                true
            }

            if (!data.channel.isMuted() || data.selected) return@after

            val itemView = (param.thisObject as WidgetChannelsListAdapter.ItemChannelStageVoice).itemView
            tintMutedVoiceItem(
                itemView.context,
                (param.thisObject as WidgetChannelsListAdapter.ItemChannelStageVoice).itemView,
                stageNameId,
                stageIconId
            )
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun Channel.toggleMuted(view: View) {
        val muted = !isMuted()
        mutedOverrides[id] = muted

        StoreStream.getUserGuildSettings().setChannelMuted(
            view.context,
            id,
            muted,
            null
        )

        view.post {
            (view.parent as? RecyclerView)
                ?.adapter
                ?.notifyDataSetChanged()
        }
    }

    private fun Channel.isMuted(): Boolean {
        mutedOverrides[id]?.let { return it }

        val settings =
            guildSettingsField.get(userGuildSettings)
                as Map<Long, ModelNotificationSettings>

        return settings[guildId]
            ?.getChannelOverride(id)
            ?.isMuted == true
    }

    private fun Channel.shouldHide() =
        isMuted() &&
            guildId in (hideMutedGuildsField.get(userGuildSettings) as Set<Long>)

    private fun tintMutedVoiceItem(
        context: Context,
        itemView: View,
        nameId: Int,
        iconId: Int
    ) {
        val color = ColorCompat.getThemedColor(context, R.b.colorInteractiveMuted)

        itemView.findViewById<TextView>(nameId)?.setTextColor(color)
        itemView.findViewById<ImageView>(iconId)?.imageTintList =
            ColorStateList.valueOf(color)
    }
}
