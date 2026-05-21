import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.9"
description = "Send embed messages with /embed or with a button."

aliucord {
    author(Authors.Cloudburst)
}