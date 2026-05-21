import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Adds search bar and filtering to debug logs"

aliucord {
    author(Authors.LampDelivery)
}