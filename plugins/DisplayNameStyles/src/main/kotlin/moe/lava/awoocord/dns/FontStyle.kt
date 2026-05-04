package moe.lava.awoocord.dns

private const val BASE_GFONTS = "https://github.com/google/fonts/raw/e324c91423626034f1e10098081182bb88e340db/ofl"

internal enum class FontStyle(val url: String?, val isVariable: Boolean = false) {
    Default(null),
    // -- Unused
    Bangers("$BASE_GFONTS/bangers/Bangers-Regular.ttf"),
    // Tempo
    BioRhyme("$BASE_GFONTS/biorhyme/BioRhyme%5Bwdth,wght%5D.ttf", isVariable = true),
    // Sakura
    CherryBomb("$BASE_GFONTS/cherrybombone/CherryBombOne-Regular.ttf"),
    // Jellybean
    Chicle("$BASE_GFONTS/chicle/Chicle-Regular.ttf"),
    // -- Unused
    Compagnon("https://gitlab.com/velvetyne/compagnon/-/raw/4f2344df5adb6eaf9ffd9215c5406c0729fb7aa1/fonts/Compagnon-Medium.otf?inline=false"),
    // Modern
    MuseoModerno("$BASE_GFONTS/museomoderno/MuseoModerno%5Bwght%5D.ttf", isVariable = true),
    // Medieval
    NeoCastel("https://files.catbox.moe/npwf2e.otf"),
    // 8Bit
    Pixelify("$BASE_GFONTS/pixelifysans/PixelifySans%5Bwght%5D.ttf", isVariable = true),
    // -- Unused
    Ribes("https://github.com/collletttivo/ribes/raw/e5f58f6ef719ff69b599a3155c66f4cecaed0a0f/fonts/Ribes-Black.otf"),
    // Vampyre
    Sinistre("https://github.com/collletttivo/sinistre/raw/3308f8b884a066951a4da38586abae3b247bf915/fonts/Sinistre-Bold.otf"),
    // -- Unused (near identical to BioRhyme)
    ZillaSlab("$BASE_GFONTS/zillaslab/ZillaSlab-Bold.ttf"),
    ;

    companion object {
        fun from(value: Int) = when (value) {
            1 -> Bangers
            2 -> BioRhyme
            3 -> CherryBomb
            4 -> Chicle
            5 -> Compagnon
            6 -> MuseoModerno
            7 -> NeoCastel
            8 -> Pixelify
            9 -> Ribes
            10 -> Sinistre
            11 -> Default
            12 -> ZillaSlab
            else -> {
                logger.warn("Unknown font style $value")
                Default
            }
        }
    }
}
