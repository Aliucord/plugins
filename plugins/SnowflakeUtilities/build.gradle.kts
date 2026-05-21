import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.6"
description = "Adds a /snowflake command to translate discord snowflakes to readable date and time"

aliucord {
    changelog.set("""
        # 1.0.6 Changed the /timestamp to /snowflake because conflicted with existing plugin
        # 1.0.4
        * Created a separate plugin for Whois
    """.trimIndent())

    author(Authors.catsoft)
}