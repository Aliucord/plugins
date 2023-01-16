#!/bin/bash

# shellcheck disable=2164
# SC2164 cd fail

cd build
mv "$(jq -r '.distFolder' < aliucord.json)/*" .

# Delete any symlinks for safety
find . -type l -delete

# Delete the ignoredPlugins as marked in aliucord.json
jq -r '.ignoredPlugins | map("'\''\(.)'.zip\'' '\''\(.)'-manifest.json\''") | join(" ")' < aliucord.json | xargs rm || true
