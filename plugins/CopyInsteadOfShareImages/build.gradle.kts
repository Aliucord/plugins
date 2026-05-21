import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.8"
description = "Replaces the share button in the image preview with one that copies the image to the clipboard."

aliucord {
    author(Authors.Accelerator)
}