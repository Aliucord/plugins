package com.github.ushie

import android.text.InputType
import android.view.View
import android.widget.Button
import com.aliucord.Utils.promptRestart
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("New Member Badge")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()

        val daysInput = TextInput(
            ctx,
            "Days",
            settings.getInt("days", 7).toString()
        ).apply {
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        addView(daysInput)

        addView(
            Button(ctx).apply {
                text = "Reset to default"
                setOnClickListener {
                    daysInput.editText.setText("7")
                    settings.setInt("days", 7)
                    promptRestart()
                }
            }
        )

        addView(
            Button(ctx).apply {
                text = "Save and restart"
                setOnClickListener {
                    val value = daysInput.editText.text.toString().toIntOrNull() ?: 7
                    settings.setInt("days", value)
                    promptRestart()
                }
            }
        )
    }
}
