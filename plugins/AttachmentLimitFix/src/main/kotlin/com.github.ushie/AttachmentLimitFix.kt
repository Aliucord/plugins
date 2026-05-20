package com.github.ushie

import android.content.Context
import com.aliucord.Utils.showToast
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.before
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.widgets.chat.input.ChatInputViewModel

@Suppress("unused")
@AliucordPlugin
class AttachmentLimitFix : Plugin() {
	override fun start(context: Context) {
        patcher.before<ChatInputViewModel>(
            "sendMessage",
            Context::class.java,
            MessageManager::class.java,
            MessageContent::class.java,
            List::class.java,
            Boolean::class.javaPrimitiveType!!,
            Function1::class.java
        ) {
            val attachments = it.args[3] as List<*>

            if (attachments.size > 10) {
                showToast("Maximum 10 attachments allowed. Please remove ${attachments.size - 10} to send.")
                it.result = null
                return@before
            }
        }
	}

	override fun stop(context: Context) {
		patcher.unpatchAll()
	}
}
