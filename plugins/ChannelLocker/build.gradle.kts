import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Lets you lock channels so you cant type in them anymore"

aliucord {
    author(Authors.Nyako)
}