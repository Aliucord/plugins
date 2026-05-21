import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Allows you to view new user profiles, but modernized"

aliucord {
    author(Authors.LampDelivery)
}

aliucord.changelog.set("""
    # 1.0.2
    * Light mode profiles are now supported, which means you can now flashbang yourself using Aliucord! You're welcome. 
    * Oh and dark mode profiles stay the same. Yeah.
""".trimIndent())