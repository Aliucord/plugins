package com.github.lampdelivery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView

object CardUtils {
    fun wrapInCard(
        context: Context,
        vararg children: View,
        cardColor: Int,
        radiusDp: Float = 20f,
        padDp: Float = 16f
    ): CardView {
        val density = context.resources.displayMetrics.density
        val card = CardView(context)
        card.radius = radiusDp * density
        card.cardElevation = 0f
        card.setCardBackgroundColor(cardColor)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = (8 * density).toInt()
        params.bottomMargin = (8 * density).toInt()
        card.layoutParams = params
        val pad = (padDp * density).toInt()
        card.setContentPadding(pad, pad, pad, pad)
        children.forEach { card.addView(it) }
        return card
    }
}
