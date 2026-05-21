import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "A plugin that shows how long you have been in a VC for."

aliucord {
    changelog.set("""
        # Version 1.0.3
        * Updated for new discord/aliucord version
        # Version 1.0.2
        * Fix a crash
        * Fix seconds bugging out with long times
        # Version 1.0.1
        * Fix ending timer on mute/deafen
        # Version 1.0.0
        * Initial release
    """.trimIndent())

    author(Authors.Ty)
}