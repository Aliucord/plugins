import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Make gifs download from tenor instead of mp4s" 

aliucord {
    author(Authors.Clienthax)
}
