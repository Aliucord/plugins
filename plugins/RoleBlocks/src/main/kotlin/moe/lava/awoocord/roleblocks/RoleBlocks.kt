package moe.lava.awoocord.roleblocks

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.aliucord.patcher.instead
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.accessField
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.models.member.GuildMember
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry

private val ChannelMembersListViewHolderMember.binding
        by accessField<WidgetChannelMembersListItemUserBinding>()
private val WidgetChatListAdapterItemMessage.itemName
        by accessField<TextView?>()
private val WidgetChatListAdapterItemMessage.replyName
        by accessField<TextView?>()

data class Colours(
    val fgP: Int,
    val bgP: Int,
    val fgO: Int,
    val bgO: Int,
)

@AliucordPlugin
class RoleBlocks : Plugin() {
    init {
        settingsTab = SettingsTab(RoleBlocksSettings.Page::class.java, SettingsTab.Type.PAGE)
    }

    override fun start(context: Context) {
        patchMemberList()
        patchMessageAuthor()
    }

    override fun stop(context: Context) { patcher.unpatchAll() }

    private fun patchMemberList() {
        // Patches the method that configures the username in members list
        patcher.after<ChannelMembersListViewHolderMember>(
            "bind",
            ChannelMembersListAdapter.Item.Member::class.java,
            Function0::class.java,
        ) { (_, member: ChannelMembersListAdapter.Item.Member) ->
            val presenceTextView = binding.d
            val usernameView = binding.f
            val usernameTextView = usernameView.j.c

            if (presenceTextView.visibility == View.VISIBLE) {
                usernameView.layoutParams = (usernameView.layoutParams as ConstraintLayout.LayoutParams).apply {
                    bottomMargin = 2.dp
                }
            }

            APCAUtil.configureOn(usernameTextView, member.color, Threshold.Medium)
        }
    }

    private fun patchMessageAuthor() {
        // Configures for message author username
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            itemName?.let {
                APCAUtil.configureOn(it, entry.author?.color, Threshold.Large)
            }
        }

        patcher.instead<WidgetChatListAdapterItemMessage>(
            "getAuthorTextColor",
            GuildMember::class.java,
        ) { (_, member: GuildMember?) ->
            member?.color ?: Color.BLACK
        }

        // Configures for reply preview username
        patcher.after<WidgetChatListAdapterItemMessage>(
            "configureReplyName",
            String::class.java,
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (_, _: String, colour: Int) ->
            replyName?.let {
                APCAUtil.configureOn(it, colour, Threshold.Small)
            }
        }
    }
}
