import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Adds a context menu to attachments."
version = "1.0.2"

aliucord {
    author(Authors.Xinto)
}
