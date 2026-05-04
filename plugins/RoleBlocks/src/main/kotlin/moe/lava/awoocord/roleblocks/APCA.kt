package moe.lava.awoocord.roleblocks

import kotlin.math.abs
import kotlin.math.pow

// https://github.com/Myndex/apca-w3/blob/c012257167d822f91bc417120bdb82e1b854b4a4/src/apca-w3.js
object APCA {
    @Suppress("ConstPropertyName")
    private object SA98G {
        const val mainTRC = 2.4

        const val sRco = 0.2126729
        const val sGco = 0.7151522
        const val sBco = 0.0721750

        const val normBG = 0.56
        const val normTXT = 0.57
        const val revTXT = 0.62
        const val revBG = 0.65

        const val blkThrs = 0.022
        const val blkClmp = 1.414
        const val scaleBoW = 1.14
        const val scaleWoB = 1.14
        const val loBoWoffset = 0.027
        const val loWoBoffset = 0.027
        const val deltaYmin = 0.0005
        const val loClip = 0.1
    }

    private fun exp(c: Int) =
        (c.toDouble() / 255.0).pow(SA98G.mainTRC)

    private fun argbToY(color: Int): Double {
        val r = (color shr 16) and 0xff
        val g = (color shr 8) and 0xff
        val b = color and 0xff

        return SA98G.run {
            sRco * exp(r) + sGco * exp(g) + sBco * exp(b)
        }
    }

    fun contrast(fgC: Int, bgC: Int): Double {
        var fg = argbToY(fgC)
        var bg = argbToY(bgC)

        if (fg.coerceAtMost(bg) < 0 || fg.coerceAtLeast(bg) > 1.1)
            return 0.0

        if (fg <= SA98G.blkThrs)
            fg += (SA98G.blkThrs - fg).pow(SA98G.blkClmp)
        if (bg <= SA98G.blkThrs)
            bg += (SA98G.blkThrs - bg).pow(SA98G.blkClmp)

        if (abs(bg - fg) < SA98G.deltaYmin)
            return 0.0

        val outputContrast = if (bg > fg) {
            val sapc = (bg.pow(SA98G.normBG) - fg.pow(SA98G.normTXT)) * SA98G.scaleBoW

            if (sapc < SA98G.loClip)
                0.0
            else
                sapc - SA98G.loBoWoffset
        } else {
            val sapc = (bg.pow(SA98G.revBG) - fg.pow(SA98G.revTXT)) * SA98G.scaleWoB

            if (sapc > -SA98G.loClip)
                0.0
            else
                sapc + SA98G.loWoBoffset
        }

        return outputContrast * 100
    }
}
