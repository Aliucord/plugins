mkdir dist && for plugin in $(find . -type f -name manifest.json -exec dirname {} \; | cut -c3-); do node build.mjs "$plugin"; done;
