import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.1"
description = "Replaces share message function with a copy url one"

aliucord {
    author(Authors.catsoft)
}