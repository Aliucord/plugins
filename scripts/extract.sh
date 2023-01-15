unzip build.zip -d build

# Delete the ignoredPlugins as marked in .aliucord.json
cd build
rm `jq -r '.ignoredPlugins | map("'\''\(.)'.zip\''") | join(" ")' < .aliucord.json`
