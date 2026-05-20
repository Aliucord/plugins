import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Adds a Discord.JS slash command to search the documentation with."

aliucord {
    author(Authors.Alyxia)
    
    changelog.set(
            """
                # 1.1.0
                * Updated for v101.3
            """.trimIndent()
    )
}