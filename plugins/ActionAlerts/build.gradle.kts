import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "A plugin that alerts you for certain actions (ie being banned/kicked from a server, friend removals)"

aliucord {
    changelog.set("""
        # Version 1.0.0
        * Initial release
    """.trimIndent())

    author(Authors.Ty)
}