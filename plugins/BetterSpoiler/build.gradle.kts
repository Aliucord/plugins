import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Spoiler attachments in specific servers, channels, or users, with an option for age-restricted channels"

aliucord {
    author(Authors.Ushie)
}
