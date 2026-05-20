package com.github.ushie

import android.annotation.SuppressLint
import android.content.Context
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.instead
import com.aliucord.wrappers.users.globalName
import com.discord.models.message.Message
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterEventsHandler
import com.github.ushie.PluginSettings.Companion.defaultShowConfirmationToast

@Suppress("unused")
@AliucordPlugin
class HoldAuthorToCopyId : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    @SuppressLint("ServiceCast")
    override fun start(context: Context) {
        patcher.instead<WidgetChatListAdapterEventsHandler>(
            "onMessageAuthorLongClicked",
            Message::class.java,
            java.lang.Long::class.java
        ) { param ->
            val message = param.args[0] as Message

            if (message.isWebhook) {
                Utils.showToast("Uh oh! We can’t view details for this user")
                return@instead null
            }

            val author = message.author
            if (author != null) {
                Utils.setClipboard("Author ID", author.id.toString())
                if (settings.getBool("showConfirmationToast", defaultShowConfirmationToast)) {
                    Utils.showToast("Copied ${author.globalName ?: author.username}'s userID to clipboard")
                }
            }
        }
    }


    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
