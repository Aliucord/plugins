import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.2"
description = "vine boom"

aliucord {
    author(Authors.Nyako)
}