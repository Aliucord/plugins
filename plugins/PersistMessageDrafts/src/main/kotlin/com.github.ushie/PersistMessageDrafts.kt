package com.github.ushie

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.stores.StoreStream
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.widgets.chat.input.ChatInputViewModel
import com.discord.widgets.chat.input.MessageDraftsRepo

@Suppress("unused")
@AliucordPlugin
class PersistMessageDrafts : Plugin() {
    override fun start(context: Context) {
        patcher.after<MessageDraftsRepo>(
            "setTextChannelInput",
            Long::class.javaPrimitiveType!!,
            CharSequence::class.java
        ) {
            val channelId = it.args[0] as Long
            val text = it.args[1] as? CharSequence

            if (text.isNullOrEmpty()) {
                settings.remove(channelId.toString())
                return@after
            }

            settings.setString(channelId.toString(), text.toString())
        }

        patcher.after<MessageDraftsRepo>("getTextChannelInput", Long::class.javaPrimitiveType!!) {
            if (it.result != null) return@after

            val channelId = it.args[0] as Long
            val draft = settings.getString(channelId.toString(), null)

            if (!draft.isNullOrEmpty()) {
                it.result = draft
            }
        }

        patcher.after<ChatInputViewModel>(
            "sendMessage",
            Context::class.java,
            MessageManager::class.java,
            MessageContent::class.java,
            List::class.java,
            Boolean::class.javaPrimitiveType!!,
            Function1::class.java
        ) {
            val channelId = StoreStream.getChannelsSelected().id.toString()
            settings.remove(channelId)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
