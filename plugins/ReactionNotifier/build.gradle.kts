import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.4"
description = "Sends you in-app notifications when someone reacts to your message"

aliucord {
    author(Authors.Nyako)
}