package com.github.ushie

import android.content.Context
import android.view.View
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.before
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem

@Suppress("unused")
@AliucordPlugin
class HideEvents : Plugin() {
    override fun start(context: Context) {
        patcher.before<WidgetChannelsListAdapter.ItemGuildScheduledEvents>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChannelListItem::class.java
        ) {
            itemView.apply {
                visibility = View.GONE
                layoutParams = itemView.layoutParams.apply {
                    height = 0
                }
            }

            it.result = null
        }
    }


    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
