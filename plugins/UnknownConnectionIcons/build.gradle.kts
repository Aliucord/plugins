import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.4.1"
description = "(tries to) Add icons for connections unsupported by Discord Kotlin"

aliucord {
    author(Authors.Nyako)
}