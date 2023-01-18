#!/bin/bash

# Target branch name is update/<repo id>, set as step output
branch_name="update/$REPO_ID"
echo "branch_name=$branch_name" >> $GITHUB_OUTPUT

# Check if target branch already exists, set base branch to checkout
if git ls-remote --exit-code "$PLUGINS_REPO_URL" "$branch_name"; then
    echo "base_branch=$branch_name" >> $GITHUB_OUTPUT
else
    echo "base_branch=data" >> $GITHUB_OUTPUT
fi
