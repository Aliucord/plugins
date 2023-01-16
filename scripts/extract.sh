#!/bin/bash

# shellcheck disable=2164
# SC2164 cd fail

cd build
mv dist/* .

# Delete any symlinks for safety
find . -type l -delete

# shellcheck disable=SC2046
# SC2046 word splitting intentional
# Delete the ignoredPlugins as marked in aliucord.json
rm $(jq -r '.ignoredPlugins | map("'\''\(.)'.zip\''") | join(" ")' < aliucord.json) || exit 0 # no ignoredPlugins
