cd plugins/modern

# Make new branch if not already on it
if [ "${{ steps.test_existing.outputs.base_branch }}" == "data" ]; then
    git checkout -b ${{ steps.test_existing.outputs.branch_name }}
fi

# Get HEAD commit on target plugin repository
src_commit=`cd plugins/repositories/${{ github.event.inputs.repo_id }} && git rev-parse HEAD`

# Copy plugin stuff into plugin dir
for pluginPath in $GITHUB_WORKSPACE/build/*.zip; do
    pluginName=`basename ${plugin::-4}`
    mkdir -p $pluginName

    # owner validation
    # if symlink exists, check if the last section of target path matches repo id
    if [ -f ./$pluginName/repository ] && [ "$(readlink -f ./$pluginName/repository | basename)" != "${{ github.event.inputs.repo_id }}" ]; then
        echo "Failed validation! This repository does not own the plugin $pluginName"
        exit 1
    fi

    # copy plugin .zip
    mv $pluginPath ./$pluginName

    # copy manifest.json
    mv -T `dirname $pluginPath`/$pluginName-manifest.json ./$pluginName/manifest.json

    # make metadata.json
    # get version & changelog from manifest, hash & commit supplied as arg
    hash=`echo -n $pluginPath | sha256sum`
    cat ./$pluginName/manifest.json | jq -c --arg hash $hash --arg commit $src_commit '{hash: $hash, changelog: .changelog, commit: $commit, version: .version}' > ./$pluginName/metadata.json

    # make repo symlink
    ln -s ../repositories/${{ github.event.inputs.repo_id }} ./$pluginName/repository
done

# make updater.json
# This supplies the existing updater.json and all of the manifests, and overrides the existing with new
cat updater.json **/manifest.json | jq -cs '.[0] + (.[1:] | reduce .[] as $manifest ({}; . + {($manifest.name): {version: $manifest.version}}))' > updater.json

# make full.json
# Supplies all manifests, combines into single array and checks for duplicates
cat **/manifest.json | jq -cs 'if group_by(.name) | any(length>1) then "Duplicate manifest name key\n" | halt_error(1) else . end' > full.json

# Remove .git from copied repo
rm -rf plugins/repositories/${{ github.event.inputs.repo_id }}/.git

# Commit
cd $GITHUB_WORKSPACE/plugins
git config --local user.email "actions@github.com"
git config --local user.name "GitHub Actions"
git add *
git commit -m "build: commit ${{ github.event.inputs.repo_owner }}/${{ github.event.inputs.repo_name }}@$src_commit" || exit 0
git push -u origin ${{ steps.test_existing.outputs.branch_name }}
