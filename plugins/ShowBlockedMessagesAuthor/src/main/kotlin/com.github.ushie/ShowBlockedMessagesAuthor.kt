package com.github.ushie

import android.content.Context
import android.widget.TextView
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.wrappers.users.globalName
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemBlocked
import com.discord.widgets.chat.list.entries.BlockedMessagesEntry
import com.discord.widgets.chat.list.entries.ChatListEntry

@Suppress("unused")
@AliucordPlugin
class ShowBlockedMessagesAuthor : Plugin() {
    val log: Logger = Logger("ShowBlockedMessagesAuthor")

    override fun start(context: Context) {
        with(WidgetChatListAdapterItemBlocked::class.java) {
            patcher.patch(getDeclaredMethod("onConfigure", Int::class.java, ChatListEntry::class.java)) { param ->
                val entry = param.args[1] as BlockedMessagesEntry
                val view = param.thisObject as WidgetChatListAdapterItemBlocked

                view.itemView.findViewById<TextView>(Utils.getResId("chat_list_adapter_item_blocked", "id")).apply {
                    text =
                        "${entry.blockedCount} blocked message${if (entry.blockedCount > 1) "s" else ""} from ${entry.message.author.globalName ?: entry.message.author.username}"
                }
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
