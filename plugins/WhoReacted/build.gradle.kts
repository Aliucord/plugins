import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "0.2.3"
description = "See the avatars of the users who reacted to a message."

aliucord {
    author(Authors.js6pak)
}
