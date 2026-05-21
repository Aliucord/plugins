import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Tap on user's 'Username', 'About Me', or 'Custom Status' on their profile to copy it!"

aliucord {
    author(Authors.Link)
}