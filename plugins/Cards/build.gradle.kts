import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}
version = "1.0.1"
description = "Adds cards to context menus and settings"

aliucord {
    author(Authors.Lamp)
    deploy.set(true)
}
