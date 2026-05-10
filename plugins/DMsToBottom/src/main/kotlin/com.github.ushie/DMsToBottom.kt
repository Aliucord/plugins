package com.github.ushie

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.utils.ReflectUtils
import com.discord.databinding.WidgetGuildsListBinding
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.widgets.guilds.list.WidgetGuildListAdapter
import com.discord.widgets.guilds.list.WidgetGuildsList
import com.discord.widgets.guilds.list.WidgetGuildsListViewModel

@Suppress("unused")
@AliucordPlugin
class DMsToBottom : Plugin() {
    override fun start(context: Context) {
        patcher.patch(
            WidgetGuildsList::class.java,
            "configureUI",
            arrayOf<Class<*>>(WidgetGuildsListViewModel.ViewState::class.java),
            Hook { callFrame ->
                val viewState = callFrame.args[0] as? WidgetGuildsListViewModel.ViewState.Loaded ?: return@Hook
                val adapter = ReflectUtils.getField(callFrame.thisObject, "adapter") as? WidgetGuildListAdapter ?: return@Hook
                val items = viewState.items

                val dmItems = items.extractAll<GuildListItem.PrivateChannelItem>() + items.extractAll<GuildListItem.FriendsItem>()
                val topItems = items.extractAll<GuildListItem.HubItem>() + items.extractAll<GuildListItem.CreateItem>()
                items.extractAll<GuildListItem.DividerItem>()

                val insertAt = items.indexOfLast { it is GuildListItem.SpaceItem }.takeIf { it >= 0 } ?: (items.size - 1)

                val reordered = topItems + items.subList(0, insertAt) + GuildListItem.DividerItem.INSTANCE + dmItems + items.subList(insertAt, items.size)
                items.clear()
                items.addAll(reordered)

                adapter.setItems(items, false)

                val binding = ReflectUtils.getField(callFrame.thisObject, "binding") as? WidgetGuildsListBinding
                binding?.root?.findViewById<RecyclerView>(Utils.getResId("guild_list", "id"))?.scrollToPosition(items.size - 1)
            }
        )
    }

    private inline fun <reified T : GuildListItem> MutableList<GuildListItem?>.extractAll(): List<T> =
        filterIsInstance<T>().also { removeAll(it.toSet()) }

    override fun stop(context: Context) = patcher.unpatchAll()
}
