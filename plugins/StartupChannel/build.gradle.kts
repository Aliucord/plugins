import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Go to a certain channel on app startup"

aliucord {
    author(Authors.Nyako)
}