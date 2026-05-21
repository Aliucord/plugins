import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.41"
description = "A plugin that allows you to write text using reactions."

aliucord {
    author(Authors.ArjixWasTaken)
}
