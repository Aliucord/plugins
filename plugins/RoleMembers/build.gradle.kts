import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.4"
description = "Shows members in a specific role with /rolemembers"

aliucord {
    author(Authors.Nyako)
}