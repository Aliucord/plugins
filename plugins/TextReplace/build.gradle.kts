import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.10"
description = "Customizable text replacer."

aliucord {
    author(Authors.Cloudburst)
}