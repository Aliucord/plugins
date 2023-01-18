#!/bin/bash

# shellcheck disable=SC2164,SC2046
# SC2164 cd fail
# SC2046 word splitting (intentional with mv below)

# move dist folder contents to build root
cd build
mv $(echo $(jq -r '.distFolder' < aliucord.json)/*) .

# Delete any symlinks for safety
find . -type l -delete

# Delete the ignoredPlugins as marked in aliucord.json
jq -r '.ignoredPlugins | map("'\''\(.)'.zip\'' '\''\(.)'-manifest.json\''") | join(" ")' < aliucord.json | xargs rm || true
