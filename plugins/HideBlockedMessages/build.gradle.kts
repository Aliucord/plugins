import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.1"
description = "Completely hides blocked messages."

aliucord {
    author(Authors.Alyxia)
}