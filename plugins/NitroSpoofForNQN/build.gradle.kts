import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Allows posting nitro emotes if the server you want to post emotes from and the server you're posting to both have the NQN bot."
version = "1.0.0"

aliucord {
    author(Authors.Accelerator)
}