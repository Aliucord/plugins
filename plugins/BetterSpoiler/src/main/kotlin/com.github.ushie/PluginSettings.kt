package com.github.ushie

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Better Spoilers")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Always spoiler in age-restricted channels",
                "Automatically spoilers attachments in NSFW channels"
            ).apply {
                isChecked = settings.getBool("spoiler_nsfw_channels", false)
                setOnCheckedListener { settings.setBool("spoiler_nsfw_channels", it) }
            }
        )
    }
}
