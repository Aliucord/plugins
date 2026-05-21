import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Reaction Emoji Name"

aliucord {
    author(Authors.Cloudburst)
    author(Authors.HalalKing)
}