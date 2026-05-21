import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Adds a /whois that inputs a mention or a userid and returns the most info it can test about that user"

aliucord {
    changelog.set("""
        # 1.0.0
        * Initial release. Adds /fw (fullwidth)
    """.trimIndent())

    author(Authors.catsoft)
}