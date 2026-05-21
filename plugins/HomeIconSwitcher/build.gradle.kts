import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "switch the home icon above guild list also fresco sucks"

aliucord {
    author(Authors.Nyako)
}