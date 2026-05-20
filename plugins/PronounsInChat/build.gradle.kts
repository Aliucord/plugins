import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Shows the user pronouns in chat messages."

aliucord {
    author(Authors.Ushie)
}
