package com.github.ushie

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.api.channel.Channel
import com.discord.api.guildjoinrequest.GuildJoinRequest
import com.discord.models.member.GuildMember
import com.discord.utilities.channel.GuildChannelsInfo
import com.discord.widgets.channels.list.WidgetChannelListModel
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemCategory

@Suppress("unused", "UNCHECKED_CAST")
@AliucordPlugin
class HideEmptyCategories : Plugin() {
    override fun start(context: Context) {
        patcher.after<WidgetChannelListModel.Companion>(
            "guildListBuilder",
            Long::class.javaPrimitiveType!!, // selectedGuildId
            GuildChannelsInfo::class.java, // guild
            Map::class.java, // guildChannels
            Map::class.java, // activeJoinedGuildThreads
            Map::class.java, // joinedThreads
            Set::class.java, // channelsWithActiveThreads
            Channel::class.java, // selectedChannel
            Long::class.javaPrimitiveType!!, // selectedVoiceChannelId
            Map::class.java, // voiceStates
            Map::class.java, // mentionCounts
            Set::class.java, // unreadChannelIds
            Set::class.java, // collapsedCategories
            Map::class.java, // stageChannels
            Map::class.java, // stageInstances
            List::class.java, // guildScheduledEvents
            Boolean::class.javaPrimitiveType!!, // canCreateAnyEvent
            Boolean::class.javaPrimitiveType!!, // canSeeGuildRoleSubscriptions
            Map::class.java, // directories
            Map::class.java, // messageAcks
            Map::class.java, // directoryEvents
            GuildJoinRequest::class.java, // guildJoinRequest
            GuildMember::class.java // member
        ) { param ->
            val items = param.result as List<ChannelListItem>
            param.result = filterEmptyCategories(items)
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}

@OptIn(ExperimentalStdlibApi::class)
private fun filterEmptyCategories(items: List<ChannelListItem>): List<ChannelListItem> {
    return buildList {
        items.filterIsInstance<ChannelListItemCategory>().forEach { category ->
            val children = items
                .dropWhile { it != category }
                .drop(1)
                .takeWhile { it !is ChannelListItemCategory }

            if (category.isCollapsed || children.isNotEmpty()) {
                add(category)
                addAll(children)
            }
        }
    }
}
