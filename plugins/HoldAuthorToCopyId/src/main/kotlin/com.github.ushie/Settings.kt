package com.github.ushie

import android.os.Build
import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    companion object {
        val defaultShowConfirmationToast = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Hold Author To Copy ID")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Show confirmation toast message",
                "Shows a toast when copying to clipboard (Android 13+ already shows system confirmation)"
            ).apply {
                isChecked = settings.getBool("showConfirmationToast", defaultShowConfirmationToast)
                setOnCheckedListener { settings.setBool("showConfirmationToast", it) }
            }
        )
    }
}
