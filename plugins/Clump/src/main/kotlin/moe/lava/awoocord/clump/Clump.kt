package moe.lava.awoocord.clump

import android.content.Context
import android.view.View
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.accessField
import com.discord.api.message.MessageTypes
import com.discord.models.message.Message
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.chat.list.model.WidgetChatListModelMessages

private val WidgetChatListAdapterItemMessage.itemText by accessField<SimpleDraweeSpanTextView>()

@AliucordPlugin(requiresRestart = true)
@Suppress("unused")
class Clump : Plugin() {
    override fun start(context: Context) {
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            if (entry.type == ChatListEntry.MESSAGE_MINIMAL && entry.message.content.isNullOrEmpty()) {
                itemText.visibility = View.GONE
            }
        }
        patcher.instead<WidgetChatListModelMessages.Companion>(
            "shouldConcatMessage",
            WidgetChatListModelMessages.Items::class.java,
            Message::class.java,
            Message::class.java,
        ) { (_, items: WidgetChatListModelMessages.Items, message: Message, message2: Message?) ->
            val timeDiff = (message.timestamp?.g() ?: 0L) - (message2?.timestamp?.g() ?: 0L)
            return@instead !(
                message2 == null ||
                message2.isSystemMessage ||
                message.hasThread() ||
                message2.hasThread() ||
                message.type !in arrayOf(MessageTypes.DEFAULT, MessageTypes.LOCAL) ||
                message.author.id != message2.author.id ||
                timeDiff >= 420000 || // WidgetChatListModelMessages.MESSAGE_CONCAT_TIMESTAMP_DELTA_THRESHOLD
//                items.listItemMostRecentlyAdded.type !in arrayOf(0, 1, 4, 21) ||
//                message2.hasAttachments() ||
//                message2.hasEmbeds() ||
//                message2.mentions?.isNotEmpty() == true ||
//                message.mentions?.isNotEmpty() == true ||
//                message.hasAttachments() ||
//                message.hasEmbeds() ||
//                items.concatCount >= 5 ||
                (message.isWebhook && message.author?.username != message2.author.username)
            )
        }
    }
}
