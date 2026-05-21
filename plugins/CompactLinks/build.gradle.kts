import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
    id("com.aliucord.plugin")
    id("org.jetbrains.kotlin.android")
}

version = "1.0.0"
description = "Makes links compact like DiscordRN"

aliucord {
    author(Authors.Lamp)
    deploy.set(true)
}
