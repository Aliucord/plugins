package com.github.ushie

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.utils.lazyField
import com.aliucord.utils.lazyMethod
import com.discord.databinding.WidgetGuildsListBinding
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.widgets.guilds.list.WidgetGuildsList
import com.discord.widgets.guilds.list.WidgetGuildsListViewModel

@Suppress("unused")
@AliucordPlugin
class DMsToBottom : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private val getBinding by lazyMethod<WidgetGuildsList>("getBinding")
    private val recyclerViewField by lazyField<WidgetGuildsListBinding>("b")

    override fun start(context: Context) {
        val stackToEnd = settings.getBool("stack_end", true)

        patcher.before<WidgetGuildsList>("configureUI", WidgetGuildsListViewModel.ViewState::class.java) { param ->
            val items = (param.args[0] as? WidgetGuildsListViewModel.ViewState.Loaded)?.items ?: return@before

            val dms = items.extractAll<GuildListItem.PrivateChannelItem>() + items.extractAll<GuildListItem.FriendsItem>()
            val top = items.extractAll<GuildListItem.HubItem>() + items.extractAll<GuildListItem.CreateItem>() + items.extractAll<GuildListItem.HelpItem>()
            items.extractAll<GuildListItem.DividerItem>()

            val insertAt = items.indexOfLast { it is GuildListItem.SpaceItem }.takeIf { it >= 0 } ?: items.size

            items.addAll(insertAt, listOf(GuildListItem.DividerItem.INSTANCE) + dms)
            items.addAll(0, top)
        }

        if (stackToEnd) {
            patcher.after<WidgetGuildsList>("setupRecycler") {
                val binding = getBinding.invoke(it.thisObject)
                val recyclerView = recyclerViewField.get(binding) as? RecyclerView ?: return@after
                (recyclerView.layoutManager as? LinearLayoutManager)?.stackFromEnd = true
            }
        }
    }

    private inline fun <reified T : GuildListItem> MutableList<GuildListItem?>.extractAll(): List<T> =
        filterIsInstance<T>().also { removeAll(it.toSet()) }

    override fun stop(context: Context) = patcher.unpatchAll()
}
