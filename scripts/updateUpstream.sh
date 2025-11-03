#!/usr/bin/env bash

# file utilized in github actions to automatically update upstream

(
set -e
PS1="$"

current=$(cat gradle.properties | grep neilCommit | sed 's/neilCommit = //')
upstream=$(git ls-remote https://github.com/V-Tube-Entertainment/Neil | grep ver/1.21.10 | cut -f 1)

if [ "$current" != "$upstream" ]; then
    sed -i 's/neilCommit = .*/neilCommit = '"$upstream"'/' gradle.properties
    {
      ./gradlew applyNeilSingleFilePatchesFuzzy --stacktrace && ./gradlew rebuildNeilSingleFilePatches --stacktrace && ./gradlew applyAllPatches --stacktrace && ./gradlew build --stacktrace && ./gradlew rebuildNeilPatches --stacktrace && ./gradlew rebuildAllServerPatches --stacktrace
    } || exit

    git add .
    ./scripts/upstreamCommit.sh "$current"
fi

) || exit 1
