import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

description = "Returns a copypasta of Senator Armstrong's political speech."
version = "1.0.1"

aliucord {
    author(Authors.Xinto)
}
