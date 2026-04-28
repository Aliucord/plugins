package com.github.ushie

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.roles.RoleIconView
import com.github.ushie.ui.NewMemberBadgeResource
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@Suppress("unused")
@AliucordPlugin
class NewMemberBadge : Plugin() {
    private val newMemberBadgeId = View.generateViewId()
    private lateinit var newMemberBadgeResource: NewMemberBadgeResource

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun load(context: Context) {
        newMemberBadgeResource = NewMemberBadgeResource(resources!!)
    }

    @OptIn(ExperimentalTime::class)
    override fun start(context: Context) {
        val daysNeeded = settings.getInt("days", 7)

        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { param ->
            val entry = param.args[1] as? MessageEntry ?: return@after
            if (entry.message.isLoading) return@after

            itemView.findViewById<ImageView>(newMemberBadgeId)?.visibility = View.GONE
            val joinedAt = runCatching { entry.author.joinedAt }.getOrNull() ?: return@after

            if (Duration
                    .milliseconds(System.currentTimeMillis() - joinedAt.g())
                    .toInt(DurationUnit.DAYS) >= daysNeeded
            ) return@after

            val headerView = itemView.findViewById<ConstraintLayout>("chat_list_adapter_item_text_header")
            val roleIconView = headerView.findViewById<RoleIconView>("chat_list_adapter_item_text_role_icon")
            val botTagView = headerView.findViewById<TextView>("chat_list_adapter_item_text_tag")

            headerView.findViewById<ImageView>(newMemberBadgeId)?.apply {
                visibility = View.VISIBLE
                return@after
            }

            ImageView(context).apply {
                id = newMemberBadgeId
                visibility = View.VISIBLE
                setImageDrawable(newMemberBadgeResource.getDrawable("ic_new_member_badge_24dp"))
                contentDescription = "New Member Badge"
                setOnClickListener {
                    Utils.showToast("I'm new here, say hi!")
                }
            }.addBetween(headerView, roleIconView, botTagView)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

// https://github.com/Aliucord/aliucord/blob/cb3acaeb44c27d477d3caaecbc37d2790ecddece/Aliucord/src/main/java/com/aliucord/coreplugins/decorations/guildtags/GuildTagView.kt
fun View.addBetween(parent: ConstraintLayout, left: View, right: View): View {
    addTo(parent) {
        left.layoutParams = (left.layoutParams as ConstraintLayout.LayoutParams).apply {
            endToStart = id
        }

        right.layoutParams = (right.layoutParams as ConstraintLayout.LayoutParams).apply {
            startToEnd = id
        }

        layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
            marginStart = 4.dp
            verticalBias = 0.5f

            topToTop = PARENT_ID
            bottomToBottom = PARENT_ID
            startToEnd = left.id
            endToStart = right.id
        }
    }

    return this
}
