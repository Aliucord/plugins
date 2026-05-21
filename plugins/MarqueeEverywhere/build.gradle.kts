import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "Applies marquees to every channel name"

aliucord {
    author(Authors.HalalKing)
}