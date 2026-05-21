package com.github.lampdelivery

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class Settings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("CompactLinks Settings")
        setActionBarSubtitle(null)
        val ctx = requireContext()
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Compact message links",
                "Compacts Discord message links in chat."
            ).apply {
                isChecked = settings.getBool("compactMessageLinks", true)
                setOnCheckedListener {
                    settings.setBool("compactMessageLinks", it)
                }
            }
        )
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Compact channel links",
                "Compacts Discord channel links in chat."
            ).apply {
                isChecked = settings.getBool("compactChannelLinks", true)
                setOnCheckedListener {
                    settings.setBool("compactChannelLinks", it)
                }
            }
        )
            addView(
                Utils.createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Compact raw file links",
                    "Compacts raw file links (.json, .txt) as filename.ext."
                ).apply {
                    isChecked = settings.getBool("compactRawLinks", true)
                    setOnCheckedListener {
                        settings.setBool("compactRawLinks", it)
                    }
                }
            )
    }
}
