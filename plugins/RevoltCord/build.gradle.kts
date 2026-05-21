import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "WebViewCord but for Revolt"

aliucord {
    author(Authors.Nyako)
}