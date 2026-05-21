/*
 * ValidUser
 * Copyright (C) 2021 js6pak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package pl.js6pak.validuser

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.aliucord.CollectionUtils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.InsteadHook
import com.aliucord.patcher.PreHook
import com.aliucord.utils.RxUtils.await
import com.discord.models.user.CoreUser
import com.discord.stores.StoreStream
import com.discord.stores.StoreUser
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.rest.RestAPI
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.utilities.textprocessing.node.UserMentionNode
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook
import retrofit2.HttpException

@AliucordPlugin
class ValidUser : Plugin() {
    private val lockedUsers = HashMap<Long, ArrayList<Long>>()
    private val invalidUsers = HashSet<Long>()

    override fun start(c: Context) {
        var chatList: WidgetChatList? = null

        patcher.patch(
            WidgetChatList::class.java.getDeclaredConstructor(),
            Hook { param: XC_MethodHook.MethodHookParam ->
                chatList = param.thisObject as WidgetChatList
            })

        patcher.patch(
            WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod(
                "getMessageRenderContext",
                Context::class.java,
                MessageEntry::class.java,
                Function1::class.java
            ),
            InsteadHook patch@{ param: XC_MethodHook.MethodHookParam ->
                val context = param.args[0] as Context
                val messageEntry = param.args[1] as MessageEntry
                val function1 = param.args[2]

                (param.thisObject as WidgetChatListAdapterItemMessage).run {
                    return@patch MessageRenderContext(
                        context,
                        messageEntry.message.id, // PATCH: set message as context id instead of author
                        messageEntry.animateEmojis,
                        messageEntry.nickOrUsernames,
                        (this.adapter as WidgetChatListAdapter).data.channelNames,
                        messageEntry.roles,
                        R.b.colorTextLink,
                        `WidgetChatListAdapterItemMessage$getMessageRenderContext$1`.INSTANCE,
                        `WidgetChatListAdapterItemMessage$getMessageRenderContext$2`(this),
                        ColorCompat.getThemedColor(context, R.b.theme_chat_spoiler_bg),
                        ColorCompat.getThemedColor(context, R.b.theme_chat_spoiler_bg_visible),
                        function1 as `WidgetChatListAdapterItemMessage$getSpoilerClickHandler$1`?,
                        `WidgetChatListAdapterItemMessage$getMessageRenderContext$3`(this),
                        `WidgetChatListAdapterItemMessage$getMessageRenderContext$4`(context)
                    )
                }
            }
        )

        patcher.patch(
            UserMentionNode::class.java.getDeclaredMethod(
                "renderUserMention",
                SpannableStringBuilder::class.java,
                UserMentionNode.RenderContext::class.java
            ),
            PreHook patch@{ param: XC_MethodHook.MethodHookParam ->
                @Suppress("UNCHECKED_CAST")
                val thisObject =
                    param.thisObject as UserMentionNode<UserMentionNode.RenderContext>
                val spannableStringBuilder = param.args[0] as SpannableStringBuilder
                val renderContext = param.args[1] as UserMentionNode.RenderContext?
                    ?: return@patch

                val userId = thisObject.userId

                if (invalidUsers.contains(userId)) {
                    logger.verbose("Set [$userId] = <invalid>")
                    setInvalidUser(renderContext.context, spannableStringBuilder, userId)
                    param.result = null
                    return@patch
                }

                if (renderContext.userNames == null || !renderContext.userNames.containsKey(userId)) {
                    val storeUser: StoreUser = StoreStream.getUsers()
                    val users = storeUser.users

                    if (!users.containsKey(userId)) {
                        param.result = null

                        if (lockedUsers.contains(userId)) {
                            logger.verbose("Waiting for $userId in ${renderContext.myId}")
                            lockedUsers[userId]!!.add(renderContext.myId)
                            setText(renderContext.context, spannableStringBuilder, "loading?")
                            return@patch
                        }

                        logger.verbose("Fetching $userId")
                        setText(renderContext.context, spannableStringBuilder, "loading")

                        fun refreshMessages() {
                            if (renderContext is MessageRenderContext && chatList != null) {
                                val adapter = WidgetChatList.`access$getAdapter$p`(chatList)
                                val data = adapter.internalData

                                for (messageId in lockedUsers[userId]!!) {
                                    val index =
                                        CollectionUtils.findIndex(data) { e: ChatListEntry? ->
                                            e is MessageEntry && e.message.id == messageId
                                        }

                                    if (index != -1) {
                                        try {
                                            adapter.notifyItemChanged(index)
                                            logger.verbose("Refreshed message $messageId")
                                        } catch (e: Throwable) {
                                            logger.warn("Failed to refresh message $messageId: $e")
                                        }
                                    } else {
                                        logger.warn("Failed to refresh message $messageId")
                                    }
                                }
                            }
                        }

                        lockedUsers[userId] = arrayListOf(renderContext.myId)
                        storeUser.dispatcher.schedule {
                            val (user, error) = RestAPI.getApi().userGet(userId).await()

                            if (error != null) {
                                if (error is HttpException && error.a() == 404) {
                                    invalidUsers.add(userId)
                                    logger.verbose("Fetched [$userId] = <invalid>")

                                    refreshMessages()
                                } else {
                                    logger.error("Failed to fetch the user", error)
                                }

                                lockedUsers.remove(userId)
                                return@schedule
                            }

                            logger.verbose("Fetched [$userId] = ${if (user == null) "null" else CoreUser(user).username}")

                            if (user != null) {
                                storeUser.notifyUserUpdated.invoke(user)
                                refreshMessages()
                            }

                            lockedUsers.remove(userId)
                        }

                        return@patch
                    }

                    logger.verbose("Set [$userId] = ${users[userId]!!.username}")

                    if (renderContext.userNames != null) {
                        renderContext.userNames[userId] = users[userId]!!.username
                    }
                }
            }
        )
    }

    private fun setInvalidUser(
        context: Context,
        spannableStringBuilder: SpannableStringBuilder,
        userId: Long
    ) {
        setText(context, spannableStringBuilder, "<@!$userId>")
    }

    private fun setText(
        context: Context,
        spannableStringBuilder: SpannableStringBuilder,
        text: String
    ) {
        val arrayList = ArrayList<CharacterStyle>()
        val length = spannableStringBuilder.length
        arrayList.add(StyleSpan(1))
        arrayList.add(
            BackgroundColorSpan(
                ColorCompat.getThemedColor(
                    context,
                    R.b.theme_chat_mention_background
                )
            )
        )
        arrayList.add(
            ForegroundColorSpan(
                ColorCompat.getThemedColor(
                    context,
                    R.b.theme_chat_mention_foreground
                )
            )
        )
        spannableStringBuilder.append(text)
        for (characterStyle in arrayList) {
            spannableStringBuilder.setSpan(
                characterStyle,
                length,
                spannableStringBuilder.length,
                33
            )
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
