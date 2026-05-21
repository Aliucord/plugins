package com.github.lampdelivery

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting
import android.text.Editable
import android.text.TextWatcher
import com.aliucord.views.TextInput
import com.aliucord.R

class CustomTimestampSettings(private val settings: SettingsAPI) : SettingsPage() {
    private fun setPreview(formatStr: String, view: android.widget.TextView) {
        val now = System.currentTimeMillis()
        val preview = try {
            java.text.SimpleDateFormat(formatStr).format(java.util.Date(now))
        } catch (e: Throwable) {
            "Invalid format"
        }
        val spannable = android.text.SpannableStringBuilder("Formatting guide\n\nPreview: ")
        spannable.append(preview)
        spannable.setSpan(
            android.text.style.URLSpan("https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"),
            0, 16, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannable
        view.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun addTextInput(hint: String, initial: String, onChange: (String) -> Unit) {
        val ctx = requireContext()
        val textInput = TextInput(ctx)
        textInput.setHint(hint)
        val mainTextColor = textInput.getEditText().currentTextColor
        textInput.getEditText().setHintTextColor(mainTextColor)
        textInput.getEditText().setText(initial)
        textInput.getEditText().setSingleLine(false)
        textInput.getEditText().maxLines = 3
        textInput.getEditText().isSingleLine = false
        textInput.getEditText().addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onChange(s?.toString() ?: "")
            }
        })
        addView(textInput)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Message Timestamps Settings")
        setActionBarSubtitle(null)
        val ctx = requireContext()

        val todayReplacement = settings.getString("todayReplacement", "")
        val formatOptions = listOf(
            "MMM dd, yyyy",         // Mon DD, YYYY (Default)
            "dd-MM-yy",             // DD-MM-YY (two-digit year)
            "dd-MM-yyyy",           // DD-MM-YYYY
            "dd MMM yyyy",          // DD Mon YYYY
            "dd/MM/yy",             // DD/MM/YY
            "dd/MM/yyyy",           // DD/MM/YYYY
            "MM/dd/yyyy",           // MM/DD/YYYY
            "yy/MM/dd",             // YY/MM/DD (two-digit year)
            "yyyy-MM-dd",           // YYYY-MM-DD (Numerical)
            "yyyy/MM/dd"            // YYYY/MM/DD
        )
        val formatLabels = listOf(
            "Mon DD, YYYY (Default)",
            "DD-MM-YY",
            "DD-MM-YYYY",
            "DD Mon YYYY",
            "DD/MM/YY",
            "DD/MM/YYYY",
            "MM/DD/YYYY",
            "YY/MM/DD",
            "YYYY-MM-DD",
            "YYYY/MM/DD"
        )
        val defaultFormat = "MMM dd, yyyy"
        val currentFormat = settings.getString("customDateFormat", defaultFormat)
        var currentIndex = formatOptions.indexOf(currentFormat).let { if (it == -1) formatOptions.indexOf(defaultFormat) else it }

        val dateFormatSelector = com.github.lampdelivery.Selector(ctx).apply {
            setLabel("Date Format")
            setValue(formatLabels[currentIndex])
            isEnabled = true 
            setSelectorClickListener(View.OnClickListener {
                val dialog = com.github.lampdelivery.SelectDialog()
                dialog.setItems(formatLabels)
                dialog.setTitle("Select Date Format")
                dialog.setOnResultListener { which ->
                    currentIndex = which
                    setValue(formatLabels[currentIndex])
                    settings.setString("customDateFormat", formatOptions[currentIndex])
                    Utils.promptRestart("Restart required to apply changes.")
                }
                dialog.show((this@CustomTimestampSettings.parentFragmentManager ?: return@OnClickListener), "date_format_selector")
            })
        }
        addView(dateFormatSelector)

        val customFormat = settings.getString("customFormat", "")
        var previewText: android.widget.TextView? = null
        addTextInput(
            hint = "Custom Date/Time Format (overrides selector if set)",
            initial = customFormat
        ) {
            settings.setString("customFormat", it)
            previewText?.let { pv -> setPreview(if (it.isNotEmpty()) it else formatOptions[currentIndex], pv) }
            Utils.promptRestart("Restart required to apply changes.")
        }
        val customFormatSpace = android.widget.Space(ctx)
        customFormatSpace.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            (12 * ctx.resources.displayMetrics.density).toInt()
        )
        addView(customFormatSpace)

        addTextInput(
            hint = "Today Replacement (leave blank to remove)",
            initial = todayReplacement
        ) {
            settings.setString("todayReplacement", it)
            Utils.promptRestart("Restart required to apply changes.")
        }
        val space = android.widget.Space(requireContext())
        space.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            (12 * resources.displayMetrics.density).toInt()
        )
        addView(space)
        val yesterdayReplacement = settings.getString("yesterdayReplacement", "")
        addTextInput(
            hint = "Yesterday Replacement (leave blank to remove)",
            initial = yesterdayReplacement
        ) {
            settings.setString("yesterdayReplacement", it)
            Utils.promptRestart("Restart required to apply changes.")
        }

        val infoText = android.widget.TextView(requireContext()).apply {
            text = "Tip: Use %date% in the box to insert the formatted date. For custom format, see SimpleDateFormat docs."
            textSize = 12f
            setTextColor(0xFF888888.toInt())
            setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, (8 * resources.displayMetrics.density).toInt())
        }
        addView(infoText)

        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Hide 'Today at' prefix",
                "Hides the 'Today at' prefix from timestamps"
            ).apply {
                isChecked = settings.getBool("hideToday", false)
                setOnCheckedListener {
                    settings.setBool("hideToday", it)
                    Utils.promptRestart("Restart required to apply changes.")
                }
            }
        )

        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Hide 'Yesterday at' prefix",
                "Hides the 'Yesterday at' prefix from timestamps"
            ).apply {
                isChecked = settings.getBool("hideYesterday", false)
                setOnCheckedListener {
                    settings.setBool("hideYesterday", it)
                    Utils.promptRestart("Restart required to apply changes.")
                }
            }
        )

        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "24 Hour Format",
                "Use 24 hour time instead of AM/PM"
            ).apply {
                isChecked = settings.getBool("use24Hour", false)
                setOnCheckedListener {
                    settings.setBool("use24Hour", it)
                    Utils.promptRestart("Restart required to apply changes.")
                }
            }
        )

        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Show Seconds",
                "Show seconds in timestamps (e.g. 12:34:56)"
            ).apply {
                isChecked = settings.getBool("showSeconds", false)
                setOnCheckedListener {
                    settings.setBool("showSeconds", it)
                    Utils.promptRestart("Restart required to apply changes.")
                }
            }
        )

        previewText = android.widget.TextView(ctx)
        previewText!!.setTextColor(0xFF888888.toInt())
        previewText!!.textSize = 12f
        setPreview(if (customFormat.isNotEmpty()) customFormat else formatOptions[currentIndex], previewText!!)
        addView(previewText)
    }
}
