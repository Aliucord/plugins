import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Standalone version of the keyboard bugfix when using the media picker, for those that don't want to install the MediaPickerPatcher plugin."

aliucord {
    author(Authors.Accelerator)
}