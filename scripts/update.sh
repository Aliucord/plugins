#!/bin/bash

# shellcheck disable=2164,SC2193
# SC2164 cd fail
# SC2193 never equal

cd plugins/modern

# Get HEAD commit on target plugin repository
srcCommit="$(cd "../repositories/$REPO_ID" && git rev-parse HEAD)"
rm -rf "plugins/repositories/$REPO_ID/.git"

# Copy plugin stuff into plugin dir
for pluginPath in "$GITHUB_WORKSPACE"/build/*.zip; do
    dirPath="$(dirname "$pluginPath")"
    pluginName="$(basename "${pluginPath::-4}")"

    mkdir -p "$pluginName"

    # owner validation
    # if symlink exists, check if the last section of target path matches repo id
    symlinkPath="./$pluginName/repository"
    if [ -f "$symlinkPath" ] && [ "$(readlink -f "$symlinkPath" | xargs basename)" != "$REPO_ID" ]; then
        echo "Failed validation! This repository does not own the plugin $pluginName"
        exit 1
    fi

    # copy over plugin .zip
    mv "$pluginPath" "./$pluginName"

    # copy over manifest.json
    mv -T "$dirPath/$pluginName-manifest.json" "./$pluginName/manifest.json"

    # make metadata.json
    # construct metadata.json from version & changelog manifest, hash & commit supplied as arg
    hash="$(echo -n "$pluginPath" | sha256sum)"
    jq -c --arg hash "$hash" --arg commit "$srcCommit" \
      '{hash: $hash, changelog: .changelog, commit: $commit, version: .version}' \
      < "./$pluginName/manifest.json" \
      > "./$pluginName/metadata.json"

    # make repo symlink
    ln -s "../repositories/$REPO_ID" "$symlinkPath"
done

# make updater.json
# This supplies the existing updater.json and all of the manifests, and overrides the existing with new
# shellcheck disable=SC2094
# SC2094 rw in same pipeline (no partial read pipes here)
cat updater.json ./**/manifest.json | \
  jq -cs '.[0] + (.[1:] | reduce .[] as $manifest ({}; . + {($manifest.name): {version: $manifest.version}}))' \
  > updater.json

# make full.json
# Supplies all manifests, combines into single array and checks for duplicates
cat ./**/manifest.json | \
  jq -cs 'if group_by(.name) | any(length>1) then "Duplicate manifest name key\n" | halt_error(1) else . end' \
  >full.json

# Commit
cd "$GITHUB_WORKSPACE/plugins"
git config --local user.email "actions@github.com"
git config --local user.name "GitHub Actions"
git add .
git commit -m "build: commit $REPO_OWNER/$REPO_NAME@$srcCommit" || exit 0
git push -u origin "$BRANCH_NAME"
