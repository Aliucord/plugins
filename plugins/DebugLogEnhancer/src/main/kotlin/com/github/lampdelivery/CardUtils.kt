package com.github.lampdelivery

import android.content.Context
import com.google.android.material.card.MaterialCardView
import android.view.ViewGroup
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

object CardUtils {
    fun wrapInMaterialCard(context: Context, child: View): MaterialCardView {
        val card = MaterialCardView(context)
        val density = context.resources.displayMetrics.density
        card.radius = 12f * density
        card.cardElevation = 0f
        card.setCardBackgroundColor(ColorCompat.getThemedColor(context, R.b.colorBackgroundSecondary))
        val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.topMargin = (4 * density).toInt()
        params.bottomMargin = (4 * density).toInt()
        card.layoutParams = params
        val pad = (8 * density).toInt()
        card.setContentPadding(pad, pad, pad, pad)
        if (child.parent != null && child.parent is ViewGroup) {
            (child.parent as ViewGroup).removeView(child)
        }
        card.addView(child, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        return card
    }
}
