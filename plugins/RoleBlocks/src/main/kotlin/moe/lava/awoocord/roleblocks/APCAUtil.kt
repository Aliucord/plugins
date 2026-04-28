package moe.lava.awoocord.roleblocks

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.aliucord.utils.DimenUtils.dp
import com.discord.stores.StoreStream
import kotlin.math.abs

enum class Threshold {
    Large,
    Medium,
    Small
}

internal object APCAUtil {
    private val settings = RoleBlocksSettings

    internal fun configureOn(view: TextView, colour: Int?, threshold: Threshold) {
        when (settings.mode) {
            Mode.Block -> configureBlock(view, colour ?: Color.BLACK, threshold)
            Mode.RoleDot -> configureRoleDot(view, colour ?: Color.BLACK)
        }
    }

    private fun configureRoleDot(view: TextView, colour: Int) { }

    private fun configureBlock(view: TextView, colourP: Int, threshold: Threshold) {
        val isLight = StoreStream.getUserSettingsSystem().theme == "light"
        var colour = colourP
        val bcol = GradientDrawable()
        bcol.cornerRadius = 4.dp.toFloat()
        view.background = bcol
        view.setPadding(4.dp, 0, 4.dp, 0)

        if (colour == Color.BLACK) {
            if (settings.blockAlsoDefault) {
                colour = if (isLight && (settings.blockInverted || settings.blockMode == BlockMode.Unchanged)) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }
            } else {
                view.background = null
                view.setPadding(0, 0, 0, 0)
                return
            }
        }

        var (preferred, other) = if (isLight) {
            Color.WHITE to Color.BLACK
        } else {
            Color.BLACK to Color.WHITE
        }
        when (settings.blockMode) {
            BlockMode.InvertedThemeOnly -> preferred = other
            BlockMode.WhiteOnly -> preferred = Color.WHITE
            BlockMode.BlackOnly -> preferred = Color.BLACK
            BlockMode.Unchanged -> preferred = colour
            else -> {}
        }

        val colours = if (!settings.blockInverted) {
            Colours(
                fgP = preferred,
                fgO = other,
                bgP = colour,
                bgO = colour,
            )
        } else {
            Colours(
                fgP = colour,
                fgO = colour,
                bgP = preferred,
                bgO = other,
            )
        }

        val usePreferred = when (settings.blockMode) {
            BlockMode.ApcaOnly -> isApca(colours, threshold)
            BlockMode.WcagOnly -> isWcag(colours)
            BlockMode.ApcaLightWcagDark -> if (isLight) isApca(colours, threshold) else isWcag(colours)
            BlockMode.WcagLightApcaDark -> if (isLight) isWcag(colours) else isApca(colours, threshold)
            BlockMode.ThemeOnly,
            BlockMode.InvertedThemeOnly,
            BlockMode.WhiteOnly,
            BlockMode.BlackOnly,
            BlockMode.Unchanged -> true
        }

        if (usePreferred) {
            view.setTextColor(colours.fgP)
            bcol.setColor(ColorUtils.setAlphaComponent(colours.bgP, settings.alpha))
        } else {
            view.setTextColor(colours.fgO)
            bcol.setColor(ColorUtils.setAlphaComponent(colours.bgO, settings.alpha))
        }
    }

    private fun isApca(c: Colours, threshold: Threshold): Boolean {
        val cPref = abs(APCA.contrast(c.fgP, c.bgP))
        val cOth = abs(APCA.contrast(c.fgO, c.bgO))
        val thresholdValue = when (threshold) {
            Threshold.Large -> settings.blockApcaThresholdLarge
            Threshold.Medium -> settings.blockApcaThresholdMedium
            Threshold.Small -> settings.blockApcaThresholdSmall
        }
        return cPref > thresholdValue || cPref > cOth
    }

    private fun isWcag(c: Colours): Boolean {
        val cPref = ColorUtils.calculateContrast(c.fgP, c.bgP)
        val cOth = ColorUtils.calculateContrast(c.fgO, c.bgO)
        return cPref > settings.blockWcagThreshold || cPref > cOth
    }
}
