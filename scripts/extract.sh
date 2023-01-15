#!/bin/bash

# shellcheck disable=2164
# SC2164 cd fail

unzip build.zip -d build
cd build

# shellcheck disable=SC2046
# SC2046 word splitting intentional
# Delete the ignoredPlugins as marked in .aliucord.json
rm $(jq -r '.ignoredPlugins | map("'\''\(.)'.zip\''") | join(" ")' < .aliucord.json)
