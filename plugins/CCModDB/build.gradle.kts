import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.0"
description = "Adds a ccmoddb slash command to search the CrossCode mod database."

aliucord {
    author(Authors.Alyxia)
    
    changelog.set(
            """
                # 1.2.0
                * Updated for v101.3
            """.trimIndent()
    )
}