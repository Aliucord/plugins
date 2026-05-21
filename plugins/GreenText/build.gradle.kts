import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.5"
description = "Highlights text starting with '>' as green. does not highlight text preceded by a space."

aliucord {
    author(Authors.Link)
}