package com.github.ushie

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Keep Expressions Open")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()


        fun addSwitch(title: String, subtitle: String, key: String) = addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                title,
                subtitle
            ).apply {
                isChecked = settings.getBool(key, true)
                setOnCheckedListener { settings.setBool(key, it) }
            }
        )

        listOf(
            Triple("Show tray toggle button", "Show a toggle button in the expression tray", "showTrayToggleButton"),
            Triple("Show emoji toggle button", "Show a toggle button in the emoji picker", "showEmojiToggleButton"),
            Triple(
                "Keep quick react open",
                "Keep the quick react menu open after selecting a reaction, this can only be toggled here for now",
                "keepQuickReactOpen"
            ),
            Triple(
                "Keep emoji picker open",
                "Keep the emoji picker open after selecting an emoji",
                "keepEmojiPickerOpen"
            ),
            Triple(
                "Keep GIF picker open",
                "Keep the GIF picker open after selecting a GIF",
                "keepGifPickerOpen"
            ),
            Triple(
                "Keep stickers open",
                "Keep the sticker picker open after selecting a sticker",
                "keepStickerPickerOpen"
            ),
        ).forEach { (title, subtitle, key) ->
            addSwitch(title, subtitle, key)
        }
    }
}
