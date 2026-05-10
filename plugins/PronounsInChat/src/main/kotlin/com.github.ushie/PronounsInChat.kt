package com.github.ushie

import android.content.Context
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

@Suppress("unused")
@AliucordPlugin
class PronounsInChat : Plugin() {
    private val sheetProfileHeaderViewId =
        Utils.getResId("user_sheet_profile_header_view", "id")
    private val timeStampViewId =
        Utils.getResId("chat_list_adapter_item_text_timestamp", "id")

    private val cache = HashMap<Long, String?>()
    private val log = com.aliucord.Logger("PronounsInChat")

    override fun start(context: Context) {

        patcher.after<UserProfileHeaderView>(
            "configureSecondaryName",
            UserProfileHeaderViewModel.ViewState.Loaded::class.java
        ) {
            val state =
                it.args[0] as? UserProfileHeaderViewModel.ViewState.Loaded
                    ?: return@after

            val profile =
                state.userProfile as? RNUserProfile
                    ?: return@after

            val userId = profile.g().id

            val pronouns =
                profile.guildMemberProfile?.pronouns?.ifEmpty { null }
                    ?: profile.userProfile?.pronouns?.ifEmpty { null }
                    ?: return@after

            savePronouns(userId, pronouns)
            cache[userId] = pronouns
        }

        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { param ->
            val entry = param.args[1] as MessageEntry
            val message = entry.message
            if (message.isLoading) return@after

            val authorId = message.author.id

            val timestampView =
                itemView.findViewById<TextView>(timeStampViewId)
                    ?: return@after

            cache[authorId]?.let {
                setPronounsTextView(timestampView, it)
                return@after
            }

            getPronouns(authorId)?.let {
                cache[authorId] = it
                setPronounsTextView(timestampView, it)
                return@after
            }
        }
    }

    private fun pronounKey(userId: Long) =
        "pronouns.user.$userId"

    private fun savePronouns(userId: Long, pronouns: String) {
        val key = pronounKey(userId)
        if (settings.getString(key, null) != pronouns) {
            settings.setString(key, pronouns)
        }
    }

    private fun getPronouns(userId: Long): String? =
        settings.getString(pronounKey(userId), null)

    private fun setPronounsTextView(
        textView: TextView,
        pronouns: String
    ) {
        if (!textView.text.contains("•")) {
            textView.text = "${textView.text} • $pronouns"
        }
    }

    override fun stop(context: Context) {
        cache.clear()
        patcher.unpatchAll()
    }
}
