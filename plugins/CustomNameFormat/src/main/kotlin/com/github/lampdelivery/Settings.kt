package com.github.lampdelivery

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.views.TextInput
import com.discord.views.CheckedSetting
import com.discord.views.RadioManager
import com.lytefast.flexinput.R

class Settings(private val settings: com.aliucord.api.SettingsAPI) : SettingsPage() {

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Custom Name Format")
        setActionBarSubtitle(null)
        val context = requireContext()

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Display Format"
                gravity = Gravity.START
            }
        )

        val formatRadios = listOf(
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Default",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Username Only",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Display Name (Username)",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Username (Display Name)",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Nickname (Username)",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Username (Nickname)",
                null
            )
        )

        val formatManager = RadioManager(formatRadios)
        val currentFormat = CustomNameFormat.Format.valueOf(
            settings.getString("format", CustomNameFormat.Format.DEFAULT.name)
        )

        for ((i, radio) in formatRadios.withIndex()) {
            radio.e {
                settings.setString("format", CustomNameFormat.Format.values()[i].name)
                formatManager.a(radio)
            }
            addView(radio)
            if (i == currentFormat.ordinal) formatManager.a(radio)
        }

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Separator Style"
                gravity = Gravity.START
            }
        )

        val separatorRadios = listOf(
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Parentheses: Name (username)",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Square Brackets: Name [username]",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Pipes: Name | username",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Bullets: Name • username",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Dashes: Name - username",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Custom Separator",
                null
            )
        )

        val separatorManager = RadioManager(separatorRadios)
        val currentSeparator = try {
            CustomNameFormat.Separator.valueOf(
                settings.getString("separator", CustomNameFormat.Separator.PARENTHESES.name)
            )
        } catch (e: IllegalArgumentException) {
            settings.setString("separator", CustomNameFormat.Separator.PARENTHESES.name)
            CustomNameFormat.Separator.PARENTHESES
        }

        val customSeparatorContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 16)
        }

        val customSeparatorInput = TextInput(context).apply {
            setHint("Custom separator (e.g., ':', '~', '>>>')")
            editText.setText(settings.getString("customSeparator", " - "))

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val customSep = s?.toString() ?: " - "
                    settings.setString("customSeparator", customSep)
                }
            })
        }
        customSeparatorContainer.addView(customSeparatorInput)

        for ((i, radio) in separatorRadios.withIndex()) {
            radio.e {
                settings.setString("separator", CustomNameFormat.Separator.values()[i].name)
                separatorManager.a(radio)

                if (i == CustomNameFormat.Separator.values().size - 1) {
                    customSeparatorContainer.visibility = View.VISIBLE
                } else {
                    customSeparatorContainer.visibility = View.GONE
                }
            }
            addView(radio)
            if (i == currentSeparator.ordinal) {
                separatorManager.a(radio)
                if (i == CustomNameFormat.Separator.values().size - 1) {
                    customSeparatorContainer.visibility = View.VISIBLE
                } else {
                    customSeparatorContainer.visibility = View.GONE
                }
            }
        }

        addView(customSeparatorContainer)

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Text Casing"
                gravity = Gravity.START
            }
        )

        val casingRadios = listOf(
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Keep Original",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Proper Case",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Lowercase",
                null
            ),
            Utils.createCheckedSetting(
                context,
                CheckedSetting.ViewType.RADIO,
                "Uppercase",
                null
            )
        )

        val casingManager = RadioManager(casingRadios)
        val currentCasing = CustomNameFormat.CasingMode.valueOf(
            settings.getString("casing", CustomNameFormat.CasingMode.NONE.name)
        )

        for ((i, radio) in casingRadios.withIndex()) {
            radio.e {
                settings.setString("casing", CustomNameFormat.CasingMode.values()[i].name)
                casingManager.a(radio)
            }
            addView(radio)
            if (i == currentCasing.ordinal) casingManager.a(radio)
        }

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Display Locations"
                gravity = Gravity.START
            }
        )

        val displayInChatToggle = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Display in Chat",
            "Show custom formatting in chat messages"
        )
        displayInChatToggle.isChecked = settings.getBool("displayInChat", true)
        displayInChatToggle.setOnCheckedListener { settings.setBool("displayInChat", it) }
        addView(displayInChatToggle)

        val displayInMemberListToggle = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Display in Member List",
            "Show custom formatting in the member list"
        )
        displayInMemberListToggle.isChecked = settings.getBool("displayInMemberList", true)
        displayInMemberListToggle.setOnCheckedListener { settings.setBool("displayInMemberList", it) }
        addView(displayInMemberListToggle)

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Advanced Options"
                gravity = Gravity.START
            }
        )

        val smartToggle = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Smart Conditional Display",
            "Hide secondary name when identical to primary name"
        )
        smartToggle.isChecked = settings.getBool("smartConditional", true)
        smartToggle.setOnCheckedListener { settings.setBool("smartConditional", it) }
        addView(smartToggle)

        val othersOnlyToggle = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Format Others Only",
            "Only apply custom formatting to other users, not yourself"
        )
        othersOnlyToggle.isChecked = settings.getBool("formatOthersOnly", false)
        othersOnlyToggle.setOnCheckedListener { settings.setBool("formatOthersOnly", it) }
        addView(othersOnlyToggle)

        val marqueeToggle = Utils.createCheckedSetting(
            context,
            CheckedSetting.ViewType.SWITCH,
            "Marquee Scrolling",
            "Enable scrolling animation for names exceeding length limit"
        )
        marqueeToggle.isChecked = settings.getBool("enableMarquee", false)
        marqueeToggle.setOnCheckedListener { settings.setBool("enableMarquee", it) }
        addView(marqueeToggle)

        addView(
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "Length Limits"
                gravity = Gravity.START
            }
        )

        val currentLength = settings.getInt("maxLength", 20)

        val sliderContainer = LinearLayout(context, null, 0, R.i.UiKit_Settings_Item).apply {
            orientation = LinearLayout.VERTICAL
        }

        val lengthLabel = TextView(context, null, 0, R.i.UiKit_TextView).apply {
            text = "Maximum length: $currentLength characters"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                bottomMargin = 4.dp
            }
        }
        sliderContainer.addView(lengthLabel)

        val lengthSlider = SeekBar(context, null, 0, R.i.UiKit_SeekBar).apply {
            max = 90
            progress = currentLength - 10
            setPadding(12.dp, 0, 12.dp, 0)

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val newLength = progress + 10
                    lengthLabel.text = "Maximum length: $newLength characters"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val newLength = (seekBar?.progress ?: 0) + 10
                    settings.setInt("maxLength", newLength)
                }
            })
        }
        sliderContainer.addView(lengthSlider)
        addView(sliderContainer)
    }
}