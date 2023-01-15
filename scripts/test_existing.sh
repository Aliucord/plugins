#!/bin/bash

# Target branch name is update/<repo id>, set as step output
branch_name="update/${{ github.event.inputs.repo_id }}"
echo "branch_name=$branch_name" >> $GITHUB_OUTPUT

# Check if target branch already exists, set base branch to checkout
if git ls-remote --exit-code https://github.com/Aliucord/plugins.git "$branch_name"; then
    echo "base_branch=$branch_name" >> $GITHUB_OUTPUT
else
    echo "base_branch=data" >> $GITHUB_OUTPUT
fi
