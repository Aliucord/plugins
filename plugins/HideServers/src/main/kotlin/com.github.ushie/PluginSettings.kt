package com.github.ushie

import android.view.View
import android.widget.Button
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(
    private val settings: SettingsAPI
) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Hide Servers")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Show toggle in server list",
                "Adds a toggle to quickly show or hide all servers"
            ).apply {
                isChecked = settings.getBool("showVisibilityToggle", true)
                setOnCheckedListener { settings.setBool("showVisibilityToggle", it) }
            }
        )
        addView(
            Button(ctx).apply {
                text = "Clear hidden servers"
                setOnClickListener {
                    settings.remove("hiddenServers")
                }
            }
        )
        addView(
            Button(ctx).apply {
                text = "Clear hidden folders"
                setOnClickListener {
                    settings.remove("hiddenFolders")
                }
            }
        )
    }
}
