import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Stops Discord from forcing channel category names to appear in all caps"

aliucord {
    author(Authors.Ushie)
}
