package com.github.lampdelivery

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.stores.StoreStream
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import java.util.*
import kotlin.math.pow

@Suppress("unused")
@AliucordPlugin
class ModernProfiles : Plugin() {
    private fun softenColorRN(color: Int): Int {
        fun luminanceOf(c: Int): Double {
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF

            fun ch(x: Int): Double {
                val v = x / 255.0
                return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
            }
            return 0.2126 * ch(r) + 0.7152 * ch(g) + 0.0722 * ch(b)
        }

        fun mix(a: Int, b: Int, t: Double): Int {
            val ar = (a shr 16) and 0xFF
            val ag = (a shr 8) and 0xFF
            val ab = a and 0xFF
            val br = (b shr 16) and 0xFF
            val bg = (b shr 8) and 0xFF
            val bb = b and 0xFF
            val rr = kotlin.math.round(ar * (1 - t) + br * t).toInt().coerceIn(0, 255)
            val rg = kotlin.math.round(ag * (1 - t) + bg * t).toInt().coerceIn(0, 255)
            val rb = kotlin.math.round(ab * (1 - t) + bb * t).toInt().coerceIn(0, 255)
            return 0xFF000000.toInt() or (rr shl 16) or (rg shl 8) or rb
        }

        val lum = luminanceOf(color)
        return when {
            lum >= 0.85 -> {
                mix(color, 0xFFFFFFFF.toInt(), 0.04)
            }

            lum >= 0.6 -> {
                mix(color, 0xFFFFFFFF.toInt(), 0.06)
            }

            lum >= 0.3 -> {
                mix(color, 0xFF000000.toInt(), 0.06)
            }

            else -> {
                mix(color, 0xFFFFFFFF.toInt(), 0.12)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun start(context: Context) {
        settingsTab = SettingsTab(Settings::class.java).withArgs(settings)
        StoreStream.getUserSettingsSystem()
        val showCreatedAt = settings.getBool("createdAt", true)
        val showJoinedAt = settings.getBool("joinedAt", true)
        val showDaysAgo = settings.getBool("showDaysAgo", true)
        val showLastMessage = settings.getBool("lastMessage", true)
        val useProfileButtonColor = settings.getBool("profileButtonColor", true)
        val stackProfileButtons = settings.getBool("stackProfileButtons", true)

        val useGradientBackgrounds = settings.getBool("useGradientBackgrounds", true)
        val useCustomGradient = settings.getBool("useCustomGradient", false)

        patcher.after<WidgetUserSheet>(
            "configureDeveloperSection",
            WidgetUserSheetViewModel.ViewState.Loaded::class.java
        ) {
            val model = it.args[0] as WidgetUserSheetViewModel.ViewState.Loaded
            val profile = model.userProfile
            if (profile is RNUserProfile) {
                fun extractTwoColors(obj: Any?): IntArray? {
                    if (obj == null) return null
                    try {
                        val k = obj.javaClass
                        try {
                            val f = k.getDeclaredField("themeColors")
                            f.isAccessible = true
                            val v = f.get(obj) as? IntArray
                            if (v != null && v.isNotEmpty()) return if (v.size >= 2) v else intArrayOf(v[0])
                        } catch (_: Throwable) {
                        }
                        var primary: Int? = null
                        var accent: Int? = null
                        val tryNames = arrayOf("primaryColor", "accentColor", "accent", "color", "backgroundColor")
                        for (n in tryNames) {
                            try {
                                val f = k.getDeclaredField(n)
                                f.isAccessible = true
                                val vv = f.get(obj)
                                if (vv is Number) {
                                    val iv = vv.toInt()
                                    if (n.lowercase().contains("accent")) {
                                        accent = iv
                                    } else if (primary ==
                                        null
                                    ) {
                                        primary = iv
                                    }
                                }
                            } catch (_: Throwable) {
                            }
                        }
                        if (primary != null && accent != null) return intArrayOf(primary, accent)
                        if (primary != null) return intArrayOf(primary)
                        if (accent != null) return intArrayOf(accent)
                    } catch (_: Throwable) {
                    }
                    return null
                }

                val rawThemeColors =
                    extractTwoColors(profile.guildMemberProfile) ?: extractTwoColors(profile.userProfile)
                        ?: return@after

                fun mixWithWhite(c: Int, t: Double): Int {
                    val r = (c shr 16) and 0xFF
                    val g = (c shr 8) and 0xFF
                    val b = c and 0xFF
                    val rr = (r + ((255 - r) * t)).toInt().coerceIn(0, 255)
                    val rg = (g + ((255 - g) * t)).toInt().coerceIn(0, 255)
                    val rb = (b + ((255 - b) * t)).toInt().coerceIn(0, 255)
                    return (0xFF shl 24) or (rr shl 16) or (rg shl 8) or rb
                }

                val themeColors = if (rawThemeColors.size >=
                    2
                ) {
                    rawThemeColors
                } else {
                    intArrayOf(rawThemeColors[0], mixWithWhite(rawThemeColors[0], 0.12))
                }

                // Detect light mode - only if BOTH colors are very light (base1 > 0.7 AND base2 > 0.75)
                val base1Lum = ColorUtils.getLuminance(themeColors[0])
                val base2Lum = ColorUtils.getLuminance(themeColors.getOrElse(1) { themeColors[0] })
                val isLightMode = base1Lum > 0.7 && base2Lum > 0.75

                val binding = WidgetUserSheet.`access$getBinding$p`(this)
                val actionsContainer = binding.D
                val root = actionsContainer.parent.parent.parent as NestedScrollView

                actionsContainer.setBackgroundColor(0)
                binding.J.apply {
                    setBackgroundColor(0)
                    (parent as View).setBackgroundColor(0)
                }

                fun findAncestorCard(v: View): CardView? {
                    var p: Any? = v.parent
                    while (p is View) {
                        if (p is CardView) return p
                        p = p.parent
                    }
                    return null
                }

                fun isDescendant(parent: ViewGroup, child: View): Boolean {
                    var p: Any? = child.parent
                    while (p is View) {
                        if (p === parent) return true
                        p = p.parent
                    }
                    return false
                }

                fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

                val baseCardColor = if (useCustomGradient && model.isMe) {
                    val customColor1 = settings.getString("customGradientColor1", "")
                    if (customColor1.isNotEmpty()) {
                        try {
                            val color1Hex = if (customColor1.startsWith("#")) customColor1 else "#$customColor1"
                            android.graphics.Color.parseColor(color1Hex)
                        } catch (e: Exception) {
                            themeColors[0]
                        }
                    } else {
                        themeColors[0]
                    }
                } else {
                    themeColors[0]
                }

                val cardColor = (baseCardColor or 0xFF000000.toInt())
                val initialAncestorCard = findAncestorCard(binding.h)

                val initialCardViews =
                    listOf(binding.b, binding.R, binding.j, binding.n.parent as View, binding.B.parent as View)
                initialCardViews.forEach { card ->
                    if (initialAncestorCard != null && card === initialAncestorCard) return@forEach
                    if (card is CardView) {
                        card.setCardBackgroundColor(cardColor)
                        card.radius = 32f
                        card.cardElevation = 0f
                        card.maxCardElevation = 0f
                    }
                }

                fun findEditButtonsContainer(start: ViewGroup): ViewGroup? {
                    val q = ArrayDeque<ViewGroup>()
                    q.add(start)
                    while (q.isNotEmpty()) {
                        val vg = q.removeFirst()
                        var editBtnCount = 0
                        for (i in 0 until vg.childCount) {
                            val c = vg.getChildAt(i)
                            if (c is Button && c.visibility == View.VISIBLE && c.id != View.NO_ID) {
                                try {
                                    val name = vg.context.resources.getResourceEntryName(c.id)
                                    if (name.contains("profile_edit") ||
                                        name.contains("profile_identity") ||
                                        name.contains("profile_actions") ||
                                        name.contains("edit_profile")
                                    ) {
                                        editBtnCount++
                                    }
                                } catch (_: Throwable) {
                                }
                            }
                        }
                        if (editBtnCount in 1..2) return vg
                        for (i in 0 until vg.childCount) {
                            val c = vg.getChildAt(i)
                            if (c is ViewGroup) q.add(c)
                        }
                    }
                    return null
                }

                fun neutralizeView(v: View?) {
                    if (v == null) return
                    try {
                        v.setBackgroundColor(0)
                    } catch (_: Throwable) {
                    }
                    try {
                        v.background = null
                    } catch (_: Throwable) {
                    }
                    try {
                        if (v is android.widget.FrameLayout) {
                            v.foreground = null
                        } else {
                            try {
                                v.foreground = null
                            } catch (_: Throwable) {
                            }
                        }
                    } catch (_: Throwable) {
                    }
                    try {
                        v.setPadding(0, 0, 0, 0)
                    } catch (_: Throwable) {
                    }
                    if (v is ViewGroup) {
                        try {
                            v.clipToPadding = false
                        } catch (_: Throwable) {
                        }
                        try {
                            v.clipChildren = false
                        } catch (_: Throwable) {
                        }
                    }
                    try {
                        v.elevation = 0f
                    } catch (_: Throwable) {
                    }
                    try {
                        v.outlineProvider = null
                    } catch (_: Throwable) {
                    }
                    try {
                        val lp = v.layoutParams
                        if (lp is ViewGroup.MarginLayoutParams) {
                            lp.leftMargin = 0
                            lp.topMargin = 0
                            lp.rightMargin = 0
                            lp.bottomMargin = 0
                            v.layoutParams = lp
                        }
                    } catch (_: Throwable) {
                    }
                    try {
                        if (v is CardView) {
                            v.setCardBackgroundColor(0)
                            v.cardElevation = 0f
                            v.maxCardElevation = 0f
                        }
                    } catch (_: Throwable) {
                    }
                }

                fun ensurePersistentNeutral(v: View?) {
                    if (v == null) return
                    try {
                        if (v.getTag("NewProfilesNeutralizedPersistent".hashCode()) == true) return
                        neutralizeView(v)
                        v.setTag("NewProfilesNeutralizedPersistent".hashCode(), true)
                        val observer = v.viewTreeObserver
                        val listener = object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                try {
                                    neutralizeView(v)
                                } catch (_: Throwable) {
                                }
                            }
                        }
                        try {
                            observer.addOnGlobalLayoutListener(listener)
                        } catch (_: Throwable) {
                        }
                    } catch (_: Throwable) {
                    }
                }

                val container =
                    findEditButtonsContainer(root as ViewGroup) ?: findEditButtonsContainer(actionsContainer)
                if (container != null && model.isMe) {
                    container.post {
                        val buttons = mutableListOf<Button>()
                        for (i in 0 until container.childCount) {
                            val c = container.getChildAt(i)
                            if (c is Button && c.visibility == View.VISIBLE) buttons.add(c)
                        }
                        if (stackProfileButtons && buttons.size == 2) {
                            val btn1 = buttons[0]
                            val btn2 = buttons[1]
                            listOf("user_sheet_message_action_button")
                            val parent = btn1.parent as? ViewGroup
                            if (parent is android.widget.LinearLayout && parent.tag == "NewProfilesButtonsContainer") {
                            } else if (parent != null) {
                                val idx1 = parent.indexOfChild(btn1)
                                val idx2 = parent.indexOfChild(btn2)
                                val minIdx = kotlin.math.min(idx1, idx2)
                                if (parent.indexOfChild(btn1) >= 0) parent.removeView(btn1)
                                if (parent.indexOfChild(btn2) >= 0) parent.removeView(btn2)
                                val newLinear = android.widget.LinearLayout(btn1.context)
                                newLinear.orientation = android.widget.LinearLayout.VERTICAL
                                newLinear.layoutParams =
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                newLinear.tag = "NewProfilesButtonsContainer"
                                try {
                                    val sidePad = (btn1.context.resources.displayMetrics.density * 16).toInt()
                                    newLinear.setPadding(sidePad, 0, sidePad, 0)
                                } catch (_: Throwable) {
                                }
                                val ancestorCard = findAncestorCard(btn1)
                                if (ancestorCard != null) {
                                    val ancParent = ancestorCard.parent as? ViewGroup
                                    if (ancParent != null) {
                                        val ancIdx = ancParent.indexOfChild(ancestorCard)
                                        try {
                                            ancParent.removeView(ancestorCard)
                                        } catch (_: Throwable) {
                                        }
                                        ancParent.addView(newLinear, if (ancIdx >= 0) ancIdx else minIdx)
                                    } else {
                                        parent.addView(newLinear, minIdx)
                                    }
                                } else {
                                    parent.addView(newLinear, minIdx)
                                }
                                try {
                                    newLinear.setBackgroundColor(0)
                                    newLinear.background = null
                                    newLinear.setPadding(0, 0, 0, 0)
                                    newLinear.clipToPadding = false
                                    newLinear.clipChildren = false
                                    newLinear.elevation = 0f
                                    val lpCheck = newLinear.layoutParams
                                    if (lpCheck is ViewGroup.MarginLayoutParams) {
                                        lpCheck.leftMargin = 0
                                        lpCheck.topMargin = 0
                                        lpCheck.rightMargin = 0
                                        lpCheck.bottomMargin = 0
                                        newLinear.layoutParams = lpCheck
                                    }
                                } catch (_: Throwable) {
                                }
                                val gap = (btn1.context.resources.displayMetrics.density * 8).toInt()
                                val sidePad = (btn1.context.resources.displayMetrics.density * 16).toInt()
                                val lp = android.widget.LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                lp.topMargin = 0
                                lp.leftMargin = sidePad
                                lp.rightMargin = sidePad
                                newLinear.addView(btn1, lp)
                                val lp2 = android.widget.LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                lp2.topMargin = gap
                                lp2.leftMargin = sidePad
                                lp2.rightMargin = sidePad
                                newLinear.addView(btn2, lp2)
                                try {
                                    val contParent = container.parent as? ViewGroup
                                    if (contParent != null) {
                                        val contIdx = contParent.indexOfChild(container)
                                        if (contIdx >= 0) {
                                            val existingParent = newLinear.parent as? ViewGroup
                                            if (existingParent != null && existingParent !== contParent) {
                                                try {
                                                    existingParent.removeView(newLinear)
                                                } catch (_: Throwable) {
                                                }
                                            }
                                            try {
                                                contParent.removeView(container)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                contParent.addView(newLinear, contIdx)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                neutralizeView(newLinear)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                ensurePersistentNeutral(newLinear)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                neutralizeView(contParent)
                                            } catch (_: Throwable) {
                                            }
                                        }
                                    }
                                } catch (_: Throwable) {
                                }
                            }
                        } else if (buttons.size == 1) {
                            val btn = buttons[0]
                            val parent = btn.parent as? ViewGroup
                            if (parent is android.widget.LinearLayout && parent.tag == "NewProfilesButtonsContainer") {
                            } else if (parent != null) {
                                val idx = parent.indexOfChild(btn)
                                if (parent.indexOfChild(btn) >= 0) parent.removeView(btn)
                                val newLinear = android.widget.LinearLayout(btn.context)
                                newLinear.orientation = android.widget.LinearLayout.VERTICAL
                                newLinear.layoutParams =
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                newLinear.tag = "NewProfilesButtonsContainer"
                                try {
                                    val sidePad = (btn.context.resources.displayMetrics.density * 16).toInt()
                                    newLinear.setPadding(sidePad, 0, sidePad, 0)
                                } catch (_: Throwable) {
                                }
                                val ancestorCard = findAncestorCard(btn)
                                if (ancestorCard != null) {
                                    val ancParent = ancestorCard.parent as? ViewGroup
                                    if (ancParent != null) {
                                        val ancIdx = ancParent.indexOfChild(ancestorCard)
                                        try {
                                            ancParent.removeView(ancestorCard)
                                        } catch (_: Throwable) {
                                        }
                                        ancParent.addView(newLinear, if (ancIdx >= 0) ancIdx else idx)
                                    } else {
                                        parent.addView(newLinear, idx)
                                    }
                                } else {
                                    parent.addView(newLinear, idx)
                                }
                                try {
                                    newLinear.setBackgroundColor(0)
                                    newLinear.background = null
                                    newLinear.setPadding(0, 0, 0, 0)
                                    newLinear.clipToPadding = false
                                    newLinear.clipChildren = false
                                    newLinear.elevation = 0f
                                    val lpCheck = newLinear.layoutParams
                                    if (lpCheck is ViewGroup.MarginLayoutParams) {
                                        lpCheck.leftMargin = 0
                                        lpCheck.topMargin = 0
                                        lpCheck.rightMargin = 0
                                        lpCheck.bottomMargin = 0
                                        newLinear.layoutParams = lpCheck
                                    }
                                } catch (_: Throwable) {
                                }
                                val sidePad = (btn.context.resources.displayMetrics.density * 16).toInt()
                                val lp = android.widget.LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                lp.topMargin = 0
                                lp.leftMargin = sidePad
                                lp.rightMargin = sidePad
                                newLinear.addView(btn, lp)
                                try {
                                    val contParent = container.parent as? ViewGroup
                                    if (contParent != null) {
                                        val contIdx = contParent.indexOfChild(container)
                                        if (contIdx >= 0) {
                                            val existingParent = newLinear.parent as? ViewGroup
                                            if (existingParent != null && existingParent !== contParent) {
                                                try {
                                                    existingParent.removeView(newLinear)
                                                } catch (_: Throwable) {
                                                }
                                            }
                                            try {
                                                contParent.removeView(container)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                contParent.addView(newLinear, contIdx)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                neutralizeView(newLinear)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                ensurePersistentNeutral(newLinear)
                                            } catch (_: Throwable) {
                                            }
                                            try {
                                                neutralizeView(contParent)
                                            } catch (_: Throwable) {
                                            }
                                        }
                                    }
                                } catch (_: Throwable) {
                                }
                            }
                        } else {
                        }
                        if (useProfileButtonColor) {
                            for (btn in buttons) {
                                try {
                                    val density = btn.context.resources.displayMetrics.density
                                    val corner = (36f * density)
                                    val drawable = GradientDrawable().apply {
                                        cornerRadius = corner
                                        setColor(cardColor)
                                    }
                                    btn.background = drawable
                                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf(cardColor)
                                    btn.backgroundTintMode = android.graphics.PorterDuff.Mode.SRC_IN
                                    btn.elevation = 0f
                                    btn.stateListAnimator = null
                                    btn.foreground = null
                                    btn.setPadding(
                                        (12 * density).toInt(),
                                        (8 * density).toInt(),
                                        (12 * density).toInt(),
                                        (
                                            8 *
                                                density
                                        ).toInt()
                                    )
                                    (btn as? android.widget.TextView)?.setTextColor(
                                        if (isLightMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                    )
                                } catch (_: Throwable) {
                                }
                            }
                        }
                        try {
                            if (container is CardView) {
                                container.setCardBackgroundColor(0)
                            } else {
                                container.setBackgroundColor(0)
                            }
                        } catch (_: Throwable) {
                        }
                    }
                }

                fun rgbToHsl(color: Int): Triple<Double, Double, Double> {
                    val r = ((color shr 16) and 0xFF) / 255.0
                    val g = ((color shr 8) and 0xFF) / 255.0
                    val b = (color and 0xFF) / 255.0
                    val max = kotlin.math.max(r, kotlin.math.max(g, b))
                    val min = kotlin.math.min(r, kotlin.math.min(g, b))
                    var h = 0.0
                    val l = (max + min) / 2.0
                    val d = max - min
                    val s = if (d == 0.0) 0.0 else d / (1.0 - kotlin.math.abs(2.0 * l - 1.0))
                    if (d != 0.0) {
                        h = when (max) {
                            r -> ((g - b) / d) % 6.0
                            g -> ((b - r) / d) + 2.0
                            else -> ((r - g) / d) + 4.0
                        }
                        h *= 60.0
                        if (h < 0) h += 360.0
                    }
                    return Triple(h, s, l)
                }

                fun hslToRgb(h: Double, s: Double, l: Double): Int {
                    val c = (1 - kotlin.math.abs(2 * l - 1)) * s
                    val hh = h / 60.0
                    val x = c * (1 - kotlin.math.abs(hh % 2 - 1))
                    val (r1, g1, b1) = when {
                        hh < 1 -> Triple(c, x, 0.0)
                        hh < 2 -> Triple(x, c, 0.0)
                        hh < 3 -> Triple(0.0, c, x)
                        hh < 4 -> Triple(0.0, x, c)
                        hh < 5 -> Triple(x, 0.0, c)
                        else -> Triple(c, 0.0, x)
                    }
                    val m = l - c / 2.0
                    val rr = ((r1 + m) * 255.0).toInt().coerceIn(0, 255)
                    val rg = ((g1 + m) * 255.0).toInt().coerceIn(0, 255)
                    val rb = ((b1 + m) * 255.0).toInt().coerceIn(0, 255)
                    return 0xFF000000.toInt() or (rr shl 16) or (rg shl 8) or rb
                }

                val base1 = themeColors[0]
                val base2 = themeColors.getOrElse(1) { themeColors[0] }

                val backgroundDrawable = if (useGradientBackgrounds) {
                    if (useCustomGradient && model.isMe) {
                        val customColor1 = settings.getString("customGradientColor1", "")
                        val customColor2 = settings.getString("customGradientColor2", "")

                        if (customColor1.isNotEmpty() && customColor2.isNotEmpty()) {
                            try {
                                val color1Hex = if (customColor1.startsWith("#")) customColor1 else "#$customColor1"
                                val color2Hex = if (customColor2.startsWith("#")) customColor2 else "#$customColor2"
                                val finalColor1 = android.graphics.Color.parseColor(color1Hex)
                                val finalColor2 = android.graphics.Color.parseColor(color2Hex)
                                val customColors = intArrayOf(finalColor1, finalColor2)

                                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, customColors).apply {
                                    cornerRadius = 0f
                                    gradientType = GradientDrawable.LINEAR_GRADIENT
                                    setGradientCenter(0.5f, 0.5f)
                                }
                            } catch (e: Exception) {
                                val (h1, s1, l1) = rgbToHsl(base1)
                                val (h2, s2, l2) = rgbToHsl(base2)

                                val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                                val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                                val finalColors = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                                    val enhancedColor2 = hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                                    intArrayOf(gradColor1, enhancedColor2)
                                } else {
                                    intArrayOf(gradColor1, gradColor2)
                                }

                                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, finalColors).apply {
                                    cornerRadius = 0f
                                    gradientType = GradientDrawable.LINEAR_GRADIENT
                                    setGradientCenter(0.5f, 0.5f)
                                }
                            }
                        } else {
                            val (h1, s1, l1) = rgbToHsl(base1)
                            val (h2, s2, l2) = rgbToHsl(base2)

                            val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                            val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                            val finalColors = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                                val enhancedColor2 = hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                                intArrayOf(gradColor1, enhancedColor2)
                            } else {
                                intArrayOf(gradColor1, gradColor2)
                            }

                            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, finalColors).apply {
                                cornerRadius = 0f
                                gradientType = GradientDrawable.LINEAR_GRADIENT
                                setGradientCenter(0.5f, 0.5f)
                            }
                        }
                    } else {
                        val (h1, s1, l1) = rgbToHsl(base1)
                        val (h2, s2, l2) = rgbToHsl(base2)

                        val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                        val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                        val finalColors = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                            val enhancedColor2 = hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                            intArrayOf(gradColor1, enhancedColor2)
                        } else {
                            intArrayOf(gradColor1, gradColor2)
                        }

                        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, finalColors).apply {
                            cornerRadius = 0f
                            gradientType = GradientDrawable.LINEAR_GRADIENT
                            setGradientCenter(0.5f, 0.5f)
                        }
                    }
                } else {
                    val (h1, s1, l1) = rgbToHsl(base1)
                    val solidColor = hslToRgb(
                        h1,
                        (s1 * 0.95).coerceIn(0.0, 1.0),
                        (l1 * 0.90).coerceIn(0.0, 1.0)
                    )
                    GradientDrawable().apply {
                        cornerRadius = 0f
                        setColor(solidColor)
                    }
                }

                root.background = backgroundDrawable

                fun getLuminance(color: Int): Double {
                    val r = (color shr 16) and 0xFF
                    val g = (color shr 8) and 0xFF
                    val b = color and 0xFF

                    fun channel(c: Int): Double {
                        val v = c / 255.0
                        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
                    }
                    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
                }
                val avgLum = if (useGradientBackgrounds) {
                    if (useCustomGradient && model.isMe) {
                        val customColor1 = settings.getString("customGradientColor1", "")
                        val customColor2 = settings.getString("customGradientColor2", "")

                        if (customColor1.isNotEmpty() && customColor2.isNotEmpty()) {
                            try {
                                val color1Hex = if (customColor1.startsWith("#")) customColor1 else "#$customColor1"
                                val color2Hex = if (customColor2.startsWith("#")) customColor2 else "#$customColor2"
                                val finalColor1 = android.graphics.Color.parseColor(color1Hex)
                                val finalColor2 = android.graphics.Color.parseColor(color2Hex)
                                (getLuminance(finalColor1) + getLuminance(finalColor2)) / 2.0
                            } catch (e: Exception) {
                                val (h1, s1, l1) = rgbToHsl(base1)
                                val (h2, s2, l2) = rgbToHsl(base2)

                                val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                                val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                                val finalGradColor2 = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                                    hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                                } else {
                                    gradColor2
                                }

                                (getLuminance(gradColor1) + getLuminance(finalGradColor2)) / 2.0
                            }
                        } else {
                            val (h1, s1, l1) = rgbToHsl(base1)
                            val (h2, s2, l2) = rgbToHsl(base2)

                            val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                            val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                            val finalGradColor2 = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                                hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                            } else {
                                gradColor2
                            }

                            (getLuminance(gradColor1) + getLuminance(finalGradColor2)) / 2.0
                        }
                    } else {
                        val (h1, s1, l1) = rgbToHsl(base1)
                        val (h2, s2, l2) = rgbToHsl(base2)

                        val gradColor1 = hslToRgb(h1, (s1 * 0.85).coerceIn(0.0, 1.0), (l1 * 0.88).coerceIn(0.0, 1.0))
                        val gradColor2 = hslToRgb(h2, (s2 * 0.75).coerceIn(0.0, 1.0), (l2 * 1.05).coerceIn(0.0, 1.0))

                        val finalGradColor2 = if (kotlin.math.abs(gradColor1 - gradColor2) < 0x202020) {
                            hslToRgb((h2 + 15.0) % 360.0, (s2 * 0.65).coerceIn(0.0, 1.0), (l2 * 1.15).coerceIn(0.0, 1.0))
                        } else {
                            gradColor2
                        }

                        (getLuminance(gradColor1) + getLuminance(finalGradColor2)) / 2.0
                    }
                } else {
                    val (h1, s1, l1) = rgbToHsl(base1)
                    val solidColor = hslToRgb(
                        h1,
                        (s1 * 0.95).coerceIn(0.0, 1.0),
                        (l1 * 0.90).coerceIn(0.0, 1.0)
                    )
                    getLuminance(solidColor)
                }
                val baseColor = if (useCustomGradient && model.isMe) {
                    val customColor1 = settings.getString("customGradientColor1", "")
                    if (customColor1.isNotEmpty()) {
                        try {
                            val color1Hex = if (customColor1.startsWith("#")) customColor1 else "#$customColor1"
                            android.graphics.Color.parseColor(color1Hex)
                        } catch (e: Exception) {
                            themeColors[0]
                        }
                    } else {
                        themeColors[0]
                    }
                } else {
                    themeColors[0]
                }
                val (bh, bs, bl) = rgbToHsl(baseColor)
                val cardL = if (isLightMode) {
                    // Light mode: keep cards light
                    bl
                } else {
                    // Dark mode: darken cards significantly
                    when {
                        bl >= 0.75 -> (bl * 0.18).coerceIn(0.0, 1.0)
                        bl >= 0.5 -> (bl * 0.28).coerceIn(0.0, 1.0)
                        bl >= 0.25 -> (bl * 0.60).coerceIn(0.0, 1.0)
                        else -> (bl * 1.05).coerceIn(0.0, 1.0)
                    }
                }
                val cardS = (bs * 0.9).coerceIn(0.0, 1.0)
                val cardShade = hslToRgb(bh, cardS, cardL)
                val ancestorCard = findAncestorCard(binding.h)
                val cardViews = mutableSetOf<View>()
                cardViews.addAll(
                    listOf(binding.b, binding.R, binding.j, binding.n.parent as View, binding.B.parent as View)
                )
                if (ancestorCard != null) cardViews.add(ancestorCard)

                fun findAllCards(v: View) {
                    if (v is CardView) cardViews.add(v)
                    if (v is ViewGroup) {
                        for (i in 0 until v.childCount) {
                            findAllCards(v.getChildAt(i))
                        }
                    }
                }
                findAllCards(root)

                // Set text colors on card contents
                fun setCardTextColor(v: View) {
                    val textColor = if (isLightMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    if (v is android.widget.TextView) v.setTextColor(textColor)
                    if (v is ViewGroup) {
                        for (i in 0 until v.childCount) {
                            setCardTextColor(v.getChildAt(i))
                        }
                    }
                }

                cardViews.forEach { card ->
                    if (card is CardView) {
                        card.setCardBackgroundColor(cardShade)
                        card.radius = 32f
                        card.cardElevation = 0f
                        card.maxCardElevation = 0f
                        setCardTextColor(card)
                    }
                }

                try {
                    val overlayDrawable = when {
                        avgLum >= 0.85 -> android.graphics.drawable.ColorDrawable(0x00FFFFFF)

                        avgLum >= 0.7 -> android.graphics.drawable.ColorDrawable(0x08FFFFFF)

                        avgLum >= 0.45 -> android.graphics.drawable.ColorDrawable(0x10000000)

                        else -> android.graphics.drawable.ColorDrawable(0x30000000)
                    }
                    val layer = android.graphics.drawable.LayerDrawable(arrayOf(backgroundDrawable, overlayDrawable))
                    root.background = layer
                } catch (_: Throwable) {
                }

                // For light mode profiles, ensure all text is dark
                if (isLightMode) {
                    fun setAllTextColorsDark(v: View) {
                        if (v is android.widget.TextView) {
                            v.setTextColor(android.graphics.Color.BLACK)
                        }
                        if (v is ViewGroup) {
                            for (i in 0 until v.childCount) {
                                setAllTextColorsDark(v.getChildAt(i))
                            }
                        }
                    }
                    setAllTextColorsDark(root)
                }

                if (model.isMe) {
                    try {
                        val primaryColor = if (useCustomGradient) {
                            val customColor1 = settings.getString("customGradientColor1", "")
                            if (customColor1.isNotEmpty()) {
                                try {
                                    val color1Hex = if (customColor1.startsWith("#")) customColor1 else "#$customColor1"
                                    (android.graphics.Color.parseColor(color1Hex) or 0xFF000000.toInt())
                                } catch (e: Exception) {
                                    (themeColors[0] or 0xFF000000.toInt())
                                }
                            } else {
                                (themeColors[0] or 0xFF000000.toInt())
                            }
                        } else {
                            (themeColors[0] or 0xFF000000.toInt())
                        }

                        fun tintButtons(v: View) {
                            try {
                                if (v is Button
                                ) {
                                    val visible = v.visibility == View.VISIBLE
                                    if (visible) {
                                        var shouldTint = false
                                        try {
                                            if (v.id != View.NO_ID) {
                                                val nm = v.context.resources.getResourceEntryName(v.id).lowercase()
                                                if (nm.contains("message") ||
                                                    nm.contains("friend") ||
                                                    nm.contains("call") ||
                                                    nm.contains("block") ||
                                                    nm.contains("report")
                                                ) {
                                                    shouldTint = false
                                                } else if (nm.contains("edit") ||
                                                    nm.contains("profile_edit") ||
                                                    nm.contains("edit_profile") ||
                                                    nm.contains("profile_identity") ||
                                                    nm.contains("editprofile") ||
                                                    nm.contains("save")
                                                ) {
                                                    shouldTint = true
                                                }
                                            }
                                        } catch (_: Throwable) {
                                        }
                                        try {
                                            val txt =
                                                (v as? android.widget.TextView)?.text?.toString()?.lowercase() ?: ""
                                            if (txt.contains("message") ||
                                                txt.contains("add friend") ||
                                                txt.contains("call") ||
                                                txt.contains("block") ||
                                                txt.contains("report")
                                            ) {
                                                shouldTint = false
                                            } else if (txt.contains("edit") ||
                                                txt.contains("edit profile") ||
                                                txt.contains("save")
                                            ) {
                                                shouldTint = true
                                            }
                                        } catch (_: Throwable) {
                                        }
                                        if (shouldTint) {
                                            try {
                                                val density = v.context.resources.displayMetrics.density
                                                val corner = (36f * density)
                                                val gd = GradientDrawable().apply {
                                                    cornerRadius = corner
                                                    setColor(primaryColor)
                                                }
                                                v.background = gd
                                                try {
                                                    (v as? android.widget.TextView)?.setTextColor(
                                                        if (getLuminance(primaryColor) <
                                                            0.5
                                                        ) {
                                                            android.graphics.Color.WHITE
                                                        } else {
                                                            android.graphics.Color.BLACK
                                                        }
                                                    )
                                                } catch (_: Throwable) {
                                                }
                                                try {
                                                    v.setPadding(
                                                        (12 * v.context.resources.displayMetrics.density).toInt(),
                                                        (
                                                            8 *
                                                                v.context.resources.displayMetrics.density
                                                        ).toInt(),
                                                        (
                                                            12 *
                                                                v.context.resources.displayMetrics.density
                                                        ).toInt(),
                                                        (
                                                            8 *
                                                                v.context.resources.displayMetrics.density
                                                        ).toInt()
                                                    )
                                                } catch (
                                                    _: Throwable
                                                ) {
                                                }
                                            } catch (_: Throwable) {
                                            }
                                        }
                                    }
                                }
                            } catch (_: Throwable) {
                            }
                            if (v is ViewGroup) {
                                for (i in 0 until v.childCount) {
                                    try {
                                        tintButtons(v.getChildAt(i))
                                    } catch (_: Throwable) {
                                    }
                                }
                            }
                        }
                        try {
                            tintButtons(root)
                        } catch (_: Throwable) {
                        }
                        try {
                            tintButtons(actionsContainer)
                        } catch (_: Throwable) {
                        }
                    } catch (_: Throwable) {
                    }
                }

                if (showCreatedAt || showJoinedAt || showLastMessage) {
                    try {
                        UserDetailsHelper.addMemberDetails(root, model.user, showCreatedAt, showJoinedAt, showDaysAgo, showLastMessage)
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        UserDetailsHelper.cleanup()
    }
}
