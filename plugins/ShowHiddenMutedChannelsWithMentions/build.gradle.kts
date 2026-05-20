import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Shows muted channels with unread mentions when “Hide Muted Channels” is enabled"

aliucord {
    author(Authors.Ushie)
}
