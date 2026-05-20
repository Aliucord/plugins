package com.github.ushie

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.channels.list.WidgetChannelListModel


@Suppress("unused")
@AliucordPlugin
class ShowHiddenMutedChannelsWithMentions : Plugin() {
    override fun start(context: Context) {
        patcher.after<WidgetChannelListModel.Companion.TextLikeChannelData>("getHide") {
            if (this.mentionCount > 0) it.result = false
        }
    }


    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
