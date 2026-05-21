import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.4"
description = "hop on bloons when"

aliucord {
    author(Authors.Nyako)
}