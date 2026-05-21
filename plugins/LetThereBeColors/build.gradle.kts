import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.7"
description = "Personalized custom text color users with the same plugin can see and customize their own!"

aliucord {
    author(Authors.Link)
}