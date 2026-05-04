package moe.lava.awoocord.glance

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.GatewayAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.aliucord.utils.ChannelUtils
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.SerializedName
import com.aliucord.utils.accessField
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.users.globalName
import com.discord.api.message.Message
import com.discord.databinding.WidgetChannelsListItemChannelPrivateBinding
import com.discord.models.domain.ModelMessageDelete
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.DiscordParser
import com.discord.utilities.textprocessing.MessagePreprocessor
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItem
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemMessage$getMessageRenderContext$1`
import com.discord.widgets.chat.list.adapter.`WidgetChatListAdapterItemMessage$getMessageRenderContext$4`
import com.google.gson.reflect.TypeToken
import com.lytefast.flexinput.R
import java.lang.ref.WeakReference

private val WidgetChannelsListAdapter.ItemChannelPrivate.binding
        by accessField<WidgetChannelsListItemChannelPrivateBinding>()

private val responseType = TypeToken.getParameterized(List::class.java, Message::class.java).type

data class ChannelIdsPayload(
    @SerializedName("channel_ids") val channelIds: List<Long>,
)

data class MessageItem(
    val id: Long,
    val content: String?,
)

fun Message.wrap(): MessageItem {
    val author = this.e()
    val authorName = if (author.id == StoreStream.getUsers().me.id) {
        "You"
    } else {
        author.globalName ?: author.username
    }
    val content = this.i()
        .takeIf { it.isNotEmpty() }
        ?.let { content -> "$authorName: ${content.takeWhile { it != '\n' }}" }

    return MessageItem(
        id = this.o(),
        content = content,
    )
}

fun SimpleDraweeSpanTextView.renderText(content: String, other: Pair<Long, String>) {
    val me = StoreStream.getUsers().me
    val meId = me.id
    val meName = me.globalName ?: me.username
    val processor = MessagePreprocessor(meId, listOf(), null, false, 50)
    val parseChannelMessage = DiscordParser.parseChannelMessage(
        context,
        content,
        MessageRenderContext(
            context,
            meId,
            false,
            mapOf(meId to meName, other),
            StoreStream.getChannels().channelNames,
            mapOf(),
            R.b.colorTextLink,
            `WidgetChatListAdapterItemMessage$getMessageRenderContext$1`.INSTANCE,
            { },
            ColorCompat.getThemedColor(context, R.b.theme_chat_spoiler_bg),
            ColorCompat.getThemedColor(context, R.b.theme_chat_spoiler_bg_visible),
            { },
            { },
            `WidgetChatListAdapterItemMessage$getMessageRenderContext$4`(context)
        ),
        processor,
        DiscordParser.ParserOptions.DEFAULT,
        false
    )
    setDraweeSpanStringBuilder(parseChannelMessage);
}

@AliucordPlugin
class Glance : Plugin() {
    var cache = mutableMapOf<Long, MessageItem>()
    var adapterRef: WeakReference<WidgetChannelsListAdapter>? = null

    override fun stop(context: Context) { patcher.unpatchAll() }

    override fun start(context: Context) {
        GatewayAPI.onEvent<Any>("READY") { refreshAll() }
        GatewayAPI.onEvent<Any>("RESUMED") { refreshAll() }

        patcher.after<WidgetChannelsListAdapter.ItemChannelPrivate>(
            "onConfigure",
            Int::class.java,
            ChannelListItem::class.java,
        ) { (_, _: Int, item: ChannelListItemPrivate) ->
            cache[item.channel.id]?.let { msg ->
                val content = msg.content
                    ?: return@let

                val descView = binding.d
                descView.visibility = View.VISIBLE
                val user = ChannelUtils.getDMRecipient(item.channel)
                descView.renderText(content, user.id to (user.globalName ?: user.username))
            }
        }
        patcher.after<WidgetChannelsListAdapter>(
            RecyclerView::class.java,
            FragmentManager::class.java,
        ) {
            adapterRef = WeakReference(this)
        }

        patcher.before<StoreStream>(
            "handleMessageCreate",
            Message::class.java
        ) { (_, msg: Message) ->
            handleMessageUpdate(msg)
        }

        patcher.before<StoreStream>(
            "handleMessageUpdate",
            Message::class.java
        ) { (_, msg: Message) ->
            handleMessageUpdate(msg)
        }

        patcher.before<StoreStream>(
            "handleMessageDelete",
            ModelMessageDelete::class.java
        ) { (_, deleteModel: ModelMessageDelete) ->
            cache[deleteModel.channelId]?.let { msg ->
                if (msg.id in deleteModel.messageIds) {
                    cache.remove(deleteModel.channelId)
                    rerender(deleteModel.channelId)
                }
            }
        }
    }

    private fun handleMessageUpdate(msg: Message) {
        val gid = msg.m()
        if (gid == null) {
            val channelId = msg.g()

            val oldMsgId = cache[channelId]?.id ?: 0
            if (msg.o() > oldMsgId) {
                cache[channelId] = msg.wrap()
                rerender(channelId)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun refreshAll() {
        val channels = StoreStream.getChannels().getChannelsForGuild(0)
            .filterValues { it.D() == 1 } // type == Type.DM
            .keys.take(100)
        Utils.threadPool.execute {
            val res = Http.Request.newDiscordRNRequest("/channels/preload-messages", "POST")
                .executeWithJson(ChannelIdsPayload(channels))
                .json<List<Message>>(GsonUtils.gsonRestApi, responseType)
            cache = mutableMapOf(*res.map { it.g() to it.wrap() }.toTypedArray())

            Utils.mainThread.post {
                @SuppressLint("NotifyDataSetChanged") // I DONT CARE HAHAHAAHJAHAAJHDLAHD
                adapterRef?.get()?.notifyDataSetChanged()
            }
        }
    }

    private fun rerender(id: Long) {
        val adapter = adapterRef?.get() ?: return
        val idx = adapter.internalData.indexOfFirst { it.key == "3$id" }
        logger.info("found $idx for $id")
        if (idx != -1) {
            Utils.mainThread.post {
                adapter.notifyItemChanged(idx)
            }
        }
    }
}
