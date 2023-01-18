#!/bin/bash

# shellcheck disable=2164,SC2103
# SC2164 cd fail
# SC2103 ( subshell ) for cd ..

cd plugins/modern

# Get HEAD commit on target plugin repository
srcCommit="$(cd "../repositories/$REPO_ID" && git rev-parse HEAD)"
rm -rf "../repositories/$REPO_ID/.git"

# Copy plugin stuff into plugin dir
for pluginPath in "$GITHUB_WORKSPACE"/build/*.zip; do
    pluginName="$(basename "${pluginPath::-4}")"
    hash="$(sha256sum "$pluginPath" | cut -d ' ' -f 1)"

    mkdir -p "$pluginName"
    cd "$pluginName"

    # owner validation
    # if symlink exists, check if the last section of target path matches repo id
    if [ -L ./repository ] && [ "$(readlink -f ./repository | xargs basename)" != "$REPO_ID" ]; then
        echo "Failed validation! This repository does not own the plugin $pluginName"
        exit 1
    elif [ ! -L ./repository ]; then
        # make repo symlink
        ln -s "../../repositories/$REPO_ID" ./repository
    elif [ -f ./repository ]; then
      echo "Repository symlink is a regular file"
      exit 1
    fi

    # copy over plugin .zip
    mv "$pluginPath" .

    # copy over manifest.json
    mv -T "$(dirname "$pluginPath")/$pluginName-manifest.json" ./manifest.json

    # make metadata.json
    # construct metadata.json from version & changelog manifest, hash & commit supplied as arg
    jq -c --arg hash "$hash" --arg commit "$srcCommit" \
      '{hash: $hash, changelog: .changelog, commit: $commit, version: .version}' \
      < ./manifest.json \
      > ./metadata.json

    cd ..
done

# make updater.json
# This supplies the existing updater.json and all of the manifests, and overrides the existing with new
newUpdater="$(cat updater.json ./**/manifest.json | \
  jq -cs '.[0] + (.[1:] | reduce .[] as $manifest ({}; . + {($manifest.name): {version: $manifest.version}}))')"
echo "$newUpdater" > updater.json

# make full.json
# Supplies all manifests, combines into single array and checks for duplicates
cat ./**/manifest.json | \
  jq -cs 'if group_by(.name) | any(length>1) then "Duplicate manifest name key\n" | halt_error(1) else . end' \
  > full.json

# Commit
cd "$GITHUB_WORKSPACE/plugins"

function commit() {
    git config --local user.email "actions@github.com"
    git config --local user.name "GitHub Actions"
    git add .
    git commit -m "build: $REPO_OWNER/$REPO_NAME@$srcCommit" || true
    git push -u origin "$BRANCH_NAME"
}

# If new branch
if [ "$BASE_BRANCH" == "data" ]; then
  git checkout -b "$BRANCH_NAME"
  commit
  gh pr create \
    --base "data" \
    --title "update: $REPO_OWNER/$REPO_NAME" \
    --body "cc: @$REPO_OWNER"
else
  commit
fi
