import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Keep emoji, GIF, sticker, and quick reaction pickers open after use, with quick in-app toggles to switch persistent behavior anytime."

aliucord {
    author(Authors.Ushie)
}
