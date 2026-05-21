import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Adds a few charcter modification slash commands"

aliucord {
    changelog.set("""
        #1.0.2
        fixed morse translation issues
        # 1.0.1
        * Now with morse, bold, and small yay
        # 1.0.0
        * Initial release
    """.trimIndent())

    author(Authors.catsoft)
}