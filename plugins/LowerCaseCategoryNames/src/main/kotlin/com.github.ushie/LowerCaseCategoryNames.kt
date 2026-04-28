package com.github.ushie

import android.content.Context
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem

@Suppress("unused")
@AliucordPlugin
class LowerCaseCategoryNames : Plugin() {
    override fun start(ctx: Context) {
        patcher.after<WidgetChannelsListAdapter.ItemChannelCategory>(
            "onConfigure",
            Int::class.java,
            ChannelListItem::class.java
        ) {
            itemView.findViewById<TextView>(Utils.getResId("channels_item_category_name", "id")).isAllCaps = false
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
