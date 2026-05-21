import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.2"
description = "Re-adds the famous quote button in the message context menu."

aliucord {
    author(Authors.Nyako)
}