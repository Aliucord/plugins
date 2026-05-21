import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.7"
description = "BetterMarkdown for Aliucord to view markdowns live on typed!"

aliucord {
    author(Authors.Link)
}