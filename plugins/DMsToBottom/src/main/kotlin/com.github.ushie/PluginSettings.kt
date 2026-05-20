package com.github.ushie

import android.content.Context
import android.view.View
import com.aliucord.Utils
import com.aliucord.Utils.promptRestart
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("DMsToBottom")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()

        addSwitch(ctx, "stack_end", "Stack bottom", "Stack the server list to the bottom", default = true)
    }

    // https://github.com/Aliucord/aliucord/blob/4161d5eca10fdba7935efaa50338ea5522c08d7b/Aliucord/src/main/java/com/aliucord/settings/AliucordPage.kt#L83-L99
    private fun addSwitch(
        ctx: Context,
        setting: String,
        title: String,
        subtitle: String? = null,
        default: Boolean = false,
    ) {
        Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle).run {
            isChecked = settings.getBool(setting, default)
            setOnCheckedListener {
                settings.setBool(setting, it)
                promptRestart()
            }
            linearLayout.addView(this)
        }
    }
}
