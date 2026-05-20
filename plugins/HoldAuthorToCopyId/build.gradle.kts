import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Hold the avatar or username to copy user ID"

aliucord {
    author(Authors.Ushie)
}
