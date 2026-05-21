import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Appends GNU/ prefix to every word in the sentence."
version = "1.0.2"

aliucord {
    author(Authors.Xinto)
}
