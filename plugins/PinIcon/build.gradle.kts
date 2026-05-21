import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Shows a pin icon next to messages that have been pinned"

aliucord {
    author(Authors.Nyako)
}