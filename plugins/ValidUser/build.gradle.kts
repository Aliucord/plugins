import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "0.2.3"
description = "Fixes an issue where mentions sometimes become invalid-user"

aliucord {
    author(Authors.js6pak)
}
