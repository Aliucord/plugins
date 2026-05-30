package dev.rushii.plugins.typingusers

import android.content.Context
import androidx.lifecycle.ViewModelStore
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.databinding.WidgetChatOverlayBinding
import com.discord.widgets.chat.overlay.ChatTypingModel
import com.discord.widgets.chat.overlay.WidgetChatOverlay

@Suppress("unused")
@AliucordPlugin(requiresRestart = true)
class TypingUsers : Plugin() {
    internal var typingUsersModelStore = ViewModelStore()
    internal var typingUsersModelClearTask: Runnable? = null

    override fun start(context: Context) {
        val fChatOverlayBinding = WidgetChatOverlay.TypingIndicatorViewHolder::class.java
            .getDeclaredField("binding")
            .apply { isAccessible = true }

        patcher.after<WidgetChatOverlay.TypingIndicatorViewHolder>(
            "configureTyping",
            ChatTypingModel.Typing::class.java,
        ) {
            val model = it.args[0] as ChatTypingModel.Typing
            if (model.typingUsers.isEmpty()) return@after

            val binding = fChatOverlayBinding.get(this) as WidgetChatOverlayBinding
            val layout = binding.a
            layout.setOnLongClickListener { view ->
                Utils.openPageWithProxy(view.context, TypingUsersPage(this@TypingUsers))
                true
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        typingUsersModelClearTask?.run()
        typingUsersModelClearTask?.let(Utils.mainThread::removeCallbacks)
    }
}
