package dev.rushii.plugins

import android.os.Bundle
import android.view.View
import com.aliucord.Utils
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting

class BetterTmSettings(private val plugin: BetterTm) : BottomSheet() {
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        addView(
            Utils.createCheckedSetting(
                view.context,
                CheckedSetting.ViewType.SWITCH,
                "Alternative Icons",
                "Use better alternative emojis instead of the originals made white",
            ).apply {
                isChecked = with(plugin) { plugin.settings.useAlt }
                setOnCheckedListener {
                    with(plugin) { plugin.settings.useAlt = it }
                    Utils.promptRestart()
                }
            },
        )
    }
}
