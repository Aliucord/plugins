import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.4"
description = "View comic-like drawings from xkcd.com"

aliucord {
    author(Authors.Nyako)
}