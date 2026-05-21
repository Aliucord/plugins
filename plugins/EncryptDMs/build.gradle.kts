import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "0.0.1"
description = "Encryption go brrrrrrrrrrrrr"

aliucord {
    changelog.set("""
        # Version 1.0.0
        * Initial release
    """.trimIndent())

    author(Authors.Ty)
}