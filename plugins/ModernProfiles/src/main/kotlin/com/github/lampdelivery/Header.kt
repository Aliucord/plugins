package com.github.lampdelivery

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel

class Header : Plugin() {
    private val customStatusViewId = Utils.getResId("user_profile_header_custom_status", "id")
    private val profileHeaderBinding = UserProfileHeaderView::class.java.getDeclaredField("binding").apply {
        isAccessible =
            true
    }

    private val UserProfileHeaderView.binding: UserProfileHeaderViewBinding?
        get() = profileHeaderBinding[this] as? UserProfileHeaderViewBinding

    override fun start(context: Context) {
        patcher.patch(
            UserProfileHeaderView::class.java,
            "updateViewState",
            arrayOf(UserProfileHeaderViewModel.ViewState.Loaded::class.java),
            Hook { hook ->
                val view = hook.thisObject as UserProfileHeaderView
                val binding = view.binding ?: return@Hook
                val customStatus = binding.i
                val nameWrap = hook.args[0]?.let {
                    val usernameParent = binding.j.parent
                    if (usernameParent is LinearLayout) usernameParent else null
                } ?: return@Hook
                val primaryName = binding.j
                val primaryColor = extractPrimaryColor(view)
                if (primaryColor == null) return@Hook
                try {
                    if (customStatus.parent is LinearLayout &&
                        (customStatus.parent as LinearLayout).orientation == LinearLayout.HORIZONTAL
                    ) {
                        return@Hook
                    }
                    (customStatus.parent as? ViewGroup)?.removeView(customStatus)
                    val ctx = customStatus.context
                    val horiz = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams =
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                    }
                    (primaryName.parent as? ViewGroup)?.removeView(primaryName)
                    horiz.addView(primaryName)
                    val card = androidx.cardview.widget.CardView(ctx).apply {
                        val color = primaryColor ?: 0xFF222222.toInt()
                        setCardBackgroundColor(color)
                        radius = (ctx.resources.displayMetrics.density * 12)
                        cardElevation = (ctx.resources.displayMetrics.density * 2)
                        layoutParams =
                            LinearLayout
                                .LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    leftMargin = (ctx.resources.displayMetrics.density * 8).toInt()
                                    rightMargin = (ctx.resources.displayMetrics.density * 8).toInt()
                                }
                    }
                    card.addView(customStatus)
                    horiz.addView(card)
                    nameWrap.addView(horiz, 0)
                } catch (_: Throwable) {
                }
            }
        )
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun extractPrimaryColor(view: UserProfileHeaderView): Int? {
        try {
            val userField = view.javaClass.getDeclaredField("user")
            userField.isAccessible = true
            val user = userField.get(view)
            val themeColorsField = user?.javaClass?.getDeclaredField("themeColors")
            themeColorsField?.isAccessible = true
            val themeColors = themeColorsField?.get(user) as? IntArray
            if (themeColors != null && themeColors.isNotEmpty()) return themeColors[0]
        } catch (_: Throwable) {
        }
        return null
    }
}
