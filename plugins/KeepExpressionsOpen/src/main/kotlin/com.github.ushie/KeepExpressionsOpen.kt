package com.github.ushie

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.instead
import com.aliucord.settings.SettingsDelegate
import com.aliucord.settings.delegate
import com.aliucord.utils.lazyField
import com.aliucord.utils.lazyMethod
import com.discord.api.sticker.Sticker
import com.discord.models.domain.emoji.Emoji
import com.discord.stores.StoreStream
import com.discord.widgets.chat.input.emoji.EmojiPickerListener
import com.discord.widgets.chat.input.emoji.WidgetEmojiPicker
import com.discord.widgets.chat.input.emoji.WidgetEmojiPickerSheet
import com.discord.widgets.chat.input.expression.WidgetExpressionTray
import com.discord.widgets.chat.input.sticker.StickerPickerViewModel
import com.discord.widgets.chat.list.actions.`WidgetChatListActions$addReaction$1`
import com.lytefast.flexinput.R


@Suppress("unused")
@AliucordPlugin
class KeepExpressionsOpen : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private val keepEmojiPickerOpen = settings.delegate("keepEmojiPickerOpen", true)
    private val keepQuickReactOpen = settings.delegate("keepQuickReactOpen", true)
    private val keepGifPickerOpen = settings.delegate("keepGifPickerOpen", true)
    private val keepStickerPickerOpen = settings.delegate("keepStickerPickerOpen", true)
    private val showTrayToggleButton = settings.delegate("showTrayToggleButton", true)
    private val showEmojiToggleButton = settings.delegate("showEmojiToggleButton", true)

    private val traySearchIconViewId = Utils.getResId("expression_tray_search_icon", "id")
    private val emojiSearchIconViewId = Utils.getResId("emoji_search_clear", "id")
    private val emojiContentId = Utils.getResId("expression_tray_emoji_picker_content", "id")
    private val gifContentId = Utils.getResId("expression_tray_gif_picker_content", "id")
    private val addReactionIconViewId = Utils.getResId("ic_chat_list_actions_add_reaction", "id")

    private val emojiPickerListenerDelegateField by lazyField<WidgetEmojiPickerSheet>("emojiPickerListenerDelegate")
    private val cancelDialogMethod by lazyMethod<WidgetEmojiPickerSheet>("cancelDialog")

    override fun start(context: Context) {
        val offIcon = AppCompatResources.getDrawable(context, R.e.ic_remove_reaction_24dp)
        val onIcon = AppCompatResources.getDrawable(context, R.e.ic_chat_list_actions_add_reaction)

        fun bindToggle(icon: ImageView, delegate: () -> SettingsDelegate<Boolean>) {
            fun update() {
                val value by delegate()
                icon.setImageDrawable(if (value) onIcon else offIcon)
            }

            update()

            icon.setOnClickListener {
                var value by delegate()
                value = !value
                update()
            }
        }

        patcher.after<WidgetExpressionTray>("onViewBound", View::class.java) { params ->
            val show by showTrayToggleButton
            if (!show) return@after

            val root = params.args[0] as View
            val icon = root.findViewById<ImageView>(traySearchIconViewId)

            fun current() = when {
                root.findViewById<View>(emojiContentId)?.visibility == View.VISIBLE -> keepEmojiPickerOpen
                root.findViewById<View>(gifContentId)?.visibility == View.VISIBLE -> keepGifPickerOpen
                else -> keepStickerPickerOpen
            }

            bindToggle(icon, ::current)
        }

        patcher.after<WidgetEmojiPicker>("onViewBound", View::class.java) { params ->
            val show by showEmojiToggleButton
            if (!show) return@after

            val icon = (params.args[0] as View).findViewById<ImageView>(emojiSearchIconViewId)
            bindToggle(icon, ::keepEmojiPickerOpen)
        }

        patcher.instead<WidgetExpressionTray>("onGifSelected") {
            val keepOpen by keepGifPickerOpen

            if (!keepOpen && isAdded) WidgetExpressionTray.`access$getFlexInputViewModel$p`(this)
                .showKeyboardAndHideExpressionTray()

            null
        }

        patcher.instead<`WidgetChatListActions$addReaction$1`>(
            "invoke",
            Void::class.java
        ) {
            var keepOpen by keepQuickReactOpen
            StoreStream.getEmojis().onEmojiUsed(`$emoji`)
            if (!keepOpen) `this$0`.dismiss()
        }

        patcher.instead<WidgetEmojiPickerSheet>("onEmojiPicked", Emoji::class.java) {
            val keepOpen by keepEmojiPickerOpen
            val emoji = it.args[0] as Emoji

            (emojiPickerListenerDelegateField.get(this) as EmojiPickerListener).onEmojiPicked(emoji)

            if (!keepOpen) {
                cancelDialogMethod.invoke(this)
                this.dismiss()
            }
        }

        patcher.after<StickerPickerViewModel>("onStickerSelected", Sticker::class.java) {
            val keepOpen by keepStickerPickerOpen
            if (keepOpen && it.result == true) it.result = false
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}
