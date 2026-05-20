import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Shows account creation, server join, and last message dates in the selected server or DM. Tap to toggle relative time."

aliucord {
    author(Authors.Ushie)
}
