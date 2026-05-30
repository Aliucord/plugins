package dev.rushii.plugins

import android.content.Context
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.actions.WidgetChatListActions

@Suppress("unused")
@AliucordPlugin
class OneClickDelete : Plugin() {
    private val reportBtnId = Utils.getResId("dialog_chat_actions_delete", "id")

    override fun start(context: Context) {
        patcher.after<WidgetChatListActions>(
            "configureUI",
            WidgetChatListActions.Model::class.java,
        ) {
            val model = it.args[0] as WidgetChatListActions.Model

            requireView()
                .findViewById<TextView>(reportBtnId)
                .setOnClickListener {
                    dismiss()
                    StoreStream.getMessages().deleteMessage(model.message)
                }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
