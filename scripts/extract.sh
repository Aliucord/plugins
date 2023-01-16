#!/bin/bash

# shellcheck disable=2164
# SC2164 cd fail

ls -R
unzip INSECURE-DO-NOT-USE -d build
cd build

# Delete any symlinks for safety
find . -type l -delete

# shellcheck disable=SC2046
# SC2046 word splitting intentional
# Delete the ignoredPlugins as marked in aliucord.json
rm $(jq -r '.ignoredPlugins | map("'\''\(.)'.zip\''") | join(" ")' < aliucord.json)
