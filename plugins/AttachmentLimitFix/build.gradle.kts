import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Stops messages with too many attachments before they fail"

aliucord {
    author(Authors.Ushie)
}
