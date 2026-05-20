package com.github.ushie

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Avatar In Header")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()

        listOf(
            Triple("Show DM avatar", "Show the user's avatar in the header when in a DM", "showDmAvatar" to true),
            Triple("Show group avatar", "Show the group icon in the header when in a group DM", "showGroupAvatar" to true),
            Triple("Show server icon", "Show the server icon in the header when in a channel", "showServerAvatar" to false),
        ).forEach { (title, subtitle, setting) ->
            val (key, default) = setting
            addView(
                Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle).apply {
                    isChecked = settings.getBool(key, default)
                    setOnCheckedListener { settings.setBool(key, it) }
                }
            )
        }
    }
}
