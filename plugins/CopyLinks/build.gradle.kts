import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Copy channel links from the context menu"

aliucord {
    author(Authors.Lamp)
    deploy.set(true)
}
