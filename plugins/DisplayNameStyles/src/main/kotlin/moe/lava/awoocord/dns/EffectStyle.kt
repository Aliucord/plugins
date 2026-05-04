package moe.lava.awoocord.dns

internal enum class EffectStyle(val value: Int) {
    Solid(1),
    Gradient(2),
    Neon(3),
    Toon(4),
    Pop(5),
    Glow(6),
    ;

    companion object {
        fun from(value: Int) = when (value) {
            1 -> Solid
            2 -> Gradient
            3 -> Neon
            4 -> Toon
            5 -> Pop
            6 -> Glow
            else -> {
                logger.warn("Unknown effect style $value")
                Solid
            }
        }
    }
}
