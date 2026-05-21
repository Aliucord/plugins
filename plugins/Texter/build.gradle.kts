import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "A plugin to modify text formatting inspired by plugins like BetterFormattingRedux (for BetterDiscord) and texter (for Powercord)"

aliucord {
    changelog.set("""
        # Version 1.0.3
        * Updated for new discord/aliucord version
        # Version 1.0.2
        * Converted code to use kotlin
        # Version 1.0.1
        * Refactored code
        * Resolve older android compatibility issues
        # Version 1.0.0
        * Initial release
    """.trimIndent())

    author(Authors.Ty)
}