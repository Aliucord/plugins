package com.github.lampdelivery

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.discord.views.CheckedSetting

class Settings(private val settings: SettingsAPI) : SettingsPage() {

    private fun isValidHexColor(hex: String?): Boolean {
        val text = hex?.trim() ?: ""
        if (text.isEmpty()) return true
        return try {
            val colorHex = if (text.startsWith("#")) text.substring(1) else text
            if (colorHex.length != 6) return false
            Color.parseColor("#$colorHex")
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun addColorInput(hint: String, settingsKey: String, onChange: () -> Unit) {
        val ctx = requireContext()
        val textInput = TextInput(ctx).apply {
            setHint(hint)
            editText.setText(settings.getString(settingsKey, ""))

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val colorText = s?.toString()?.trim() ?: ""
                    if (isValidHexColor(colorText)) {
                        settings.setString(settingsKey, colorText)
                        onChange()
                    }
                }
            })
        }
        addView(textInput)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Modern Profiles")
        setActionBarSubtitle(null)
        val ctx = requireContext()

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Show 'Created At' info",
                    "Display when the user created their account."
                ).apply {
                    isChecked = settings.getBool("createdAt", true)
                    setOnCheckedListener {
                        settings.setBool("createdAt", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Show 'Joined At' info",
                    "Display when the user joined the server."
                ).apply {
                    isChecked = settings.getBool("joinedAt", true)
                    setOnCheckedListener {
                        settings.setBool("joinedAt", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Show days ago",
                    "Show relative time (e.g., '5 days ago') alongside dates."
                ).apply {
                    isChecked = settings.getBool("showDaysAgo", true)
                    setOnCheckedListener {
                        settings.setBool("showDaysAgo", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Show 'Last Message' info",
                    "Display when the user last sent a message in the current channel/server."
                ).apply {
                    isChecked = settings.getBool("lastMessage", true)
                    setOnCheckedListener {
                        settings.setBool("lastMessage", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Use primary color for profile buttons",
                    "Apply the primary profile color to all profile-related buttons except message action."
                ).apply {
                    isChecked = settings.getBool("profileButtonColor", true)
                    setOnCheckedListener {
                        settings.setBool("profileButtonColor", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Stack profile buttons",
                    "Stack the two profile buttons vertically instead of side-by-side."
                ).apply {
                    isChecked = settings.getBool("stackProfileButtons", true)
                    setOnCheckedListener {
                        settings.setBool("stackProfileButtons", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Use gradient backgrounds",
                    "Generate smooth gradient fades from profile colors instead of solid colors."
                ).apply {
                    isChecked = settings.getBool("useGradientBackgrounds", true)
                    setOnCheckedListener {
                        settings.setBool("useGradientBackgrounds", it)
                    }
                }
        )

        addView(
            Utils
                .createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Use custom gradient colors",
                    "Override profile colors with your own custom gradient colors."
                ).apply {
                    isChecked = settings.getBool("useCustomGradient", false)
                    setOnCheckedListener {
                        settings.setBool("useCustomGradient", it)
                    }
                }
        )

        val headerLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 16)
        }

        val headerText = TextView(ctx).apply {
            text = "Custom Gradient Colors"
            textSize = 18f
            setTextColor(0xFFE3E5E8.toInt())
            setPadding(0, 0, 0, 16)
        }
        headerLayout.addView(headerText)

        val descText = TextView(ctx).apply {
            text = "Set custom colors for gradient backgrounds (requires gradient backgrounds enabled)"
            textSize = 14f
            setTextColor(0xFFB9BBBE.toInt())
        }
        headerLayout.addView(descText)
        addView(headerLayout)

        val gradient1ColorContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val gradient1Label = TextView(ctx).apply {
            text = "Gradient Color 1 (Top)"
            textSize = 16f
            setTextColor(0xFFE3E5E8.toInt())
            setPadding(0, 0, 0, 8)
        }
        gradient1ColorContainer.addView(gradient1Label)
        addView(gradient1ColorContainer)

        addColorInput("Gradient Color 1 (e.g., #5865F2)", "customGradientColor1") {}

        val gradient2ColorContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val gradient2Label = TextView(ctx).apply {
            text = "Gradient Color 2 (Bottom)"
            textSize = 16f
            setTextColor(0xFFE3E5E8.toInt())
            setPadding(0, 0, 0, 8)
        }
        gradient2ColorContainer.addView(gradient2Label)
        addView(gradient2ColorContainer)

        addColorInput("Gradient Color 2 (e.g., #EB459E)", "customGradientColor2") {}

    }
}
