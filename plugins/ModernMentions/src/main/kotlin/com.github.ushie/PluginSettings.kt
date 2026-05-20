package com.github.ushie

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.aliucord.Utils
import com.aliucord.Utils.promptRestart
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.aliucord.views.TextInput
import com.discord.views.CheckedSetting

// https://github.com/Juby210/Aliucord-plugins/blob/ffa31ecfbd3d2e0e1e104b8c94d7d17675c6e02e/BetterStatusIndicators/src/main/java/io/github/juby210/acplugins/bsi/PluginSettings.kt#L24-L29
class SimpleTextWatcher(private val after: (Editable?) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) = after(s)
}

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle("Avatar In Mentions")
        setActionBarSubtitle("Settings")

        val ctx = requireContext()

        val paddingInput = createIntInput(ctx, "padding", "Padding", 12)
        val gapInput = createIntInput(ctx, "avatar_gap", "Avatar Gap", 8)
        val radiusInput = createIntInput(ctx, "radius", "Corner Radius", 12)
        val prefixInput = createInput(ctx, "prefix", "Mention prefix character", "@", allowEmpty = true)


        addSwitch(ctx, "show_avatar", "Show avatars", default = true)
        addSwitch(ctx, "use_role_color", "Use role colors", default = true)

        addView(paddingInput)
        addView(gapInput)
        addView(radiusInput)
        addView(prefixInput)
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
            }
            linearLayout.addView(this)
        }
    }

    // https://github.com/Juby210/Aliucord-plugins/blob/ffa31ecfbd3d2e0e1e104b8c94d7d17675c6e02e/BetterStatusIndicators/src/main/java/io/github/juby210/acplugins/bsi/PluginSettings.kt#L230-L245
    private fun createInput(
        context: Context,
        key: String,
        label: String,
        default: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        allowEmpty: Boolean = false,
        onChange: (String) -> Unit = { settings.setString(key, it) },
    ) = TextInput(
        context,
        "$label (default $default)",
        settings.getString(key, default),
        SimpleTextWatcher {
            val value = it?.toString().orEmpty()
            if (allowEmpty || value.isNotEmpty()) onChange(value)
            promptRestart()
        }
    ).apply {
        editText.inputType = inputType
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            DimenUtils.defaultPadding.let { setMargins(it, 0, it, it) }
        }
    }

    private fun createIntInput(
        context: Context,
        key: String,
        label: String,
        default: Int,
        allowEmpty: Boolean = false
    ) = createInput(
        context,
        key,
        label,
        default.toString(),
        InputType.TYPE_CLASS_NUMBER,
        allowEmpty = allowEmpty
    ) {
        settings.setInt(key, it.toInt())
    }
}
