import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Removes all roles from a selected member at once with a tap of a button!"

aliucord {
    author(Authors.Link)
}