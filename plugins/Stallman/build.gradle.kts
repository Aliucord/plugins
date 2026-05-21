import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Replace the word \"Linux\" in the Linux Copypasta with your desired phrase."
version = "1.0.3"

aliucord {
    author(Authors.Xinto)
}
