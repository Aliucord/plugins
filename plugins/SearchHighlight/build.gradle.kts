import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "0.0.1"
description = "Highlights search terms in message search results"

aliucord {
    changelog.set("""
        * initial release
    """.trimIndent())

    author(Authors.catsoft)
}