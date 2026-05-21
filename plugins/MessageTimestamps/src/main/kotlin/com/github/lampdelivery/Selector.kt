package com.github.lampdelivery

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.R

class Selector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val label: TextView
    private val value: TextView
    private var onClickListener: OnClickListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val p = (16 * resources.displayMetrics.density).toInt()
        setPadding(p, p, p, p)
        label = TextView(context)
        value = TextView(context)
        value.gravity = Gravity.END
        value.setTextColor(android.graphics.Color.WHITE) 
        value.compoundDrawablePadding = p
        addView(label, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(value, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        try {
            val themedColor = com.discord.utilities.color.ColorCompat.getThemedColor(context, com.lytefast.flexinput.R.b.colorInteractiveNormal)
            label.setTextColor(themedColor)
            value.setTextColor(themedColor)
        } catch (_: Throwable) {}
        super.setOnClickListener { onClickListener?.onClick(this) }
    }

    fun setLabel(text: String) { label.text = text }
    fun setValue(text: String) { value.text = text }
    fun setSelectorClickListener(listener: OnClickListener) { onClickListener = listener }
}
