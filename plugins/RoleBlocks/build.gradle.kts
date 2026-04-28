version = "1.2.1"
description = "Coloured usernames to be a bit more pleasing on the eyes"

aliucord {
    // Changelog of your plugin
    changelog.set("""
        # 1.2.1
        * Use correct default block colour in replies
        * Use correct default block colour in "unchanged" mode

        # 1.2.0
        * Finally fixes the annoying padding issue in replies
        * Adds nice preview blocks in settings with configurable hsv bars for all your previewing needs
        * Tweaked constrast ratio a bit which may improve some colours' legibility
        * Added transparency option, alongside "unchanged" colour option which pairs nicely together for a translucent glass effect

        # 1.1.1
        * Revert incorrect spacing fix, since it just breaks replies. Proper fix soon

        # 1.1.0
        * Fix incorrect spacing in replies
        * Allow setting static text colours

        # 1.0.0
        * Initial release >w<
    """.trimIndent())

    deploy.set(true)
}
