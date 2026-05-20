import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Persists chat drafts across app restarts/refreshes"

aliucord {
    author(Authors.Ushie)
}
