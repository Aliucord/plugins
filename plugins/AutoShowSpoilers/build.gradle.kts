import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Automatically clicks on spoilers for you"

aliucord {
    author(Authors.Nyako)
}