package com.github.ushie

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("No Server Profiles")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()

        fun addSwitch(
            title: String,
            subtitle: String,
            key: String,
            default: Boolean
        ) {
            addView(
                Utils.createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    title,
                    subtitle
                ).apply {
                    isChecked = settings.getBool(key, default)
                    setOnCheckedListener { settings.setBool(key, it) }
                }
            )
        }

        addSwitch(
            "Disable server nicknames",
            "Show display name instead of server nicknames",
            "disableNick",
            false
        )

        addSwitch(
            "Disable server avatar",
            "Show regular avatars instead of server avatars",
            "disableAvatar",
            true
        )

        addSwitch(
            "Disable server banner",
            "Show regular banners instead of server banners",
            "disableBanner",
            true
        )
    }
}
