import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Backports voice/stage channel muting. Long press to mute/unmute vocal channels and hide them when Hide Muted Channels is enabled."

aliucord {
    author(Authors.Ushie)
}
