#!/bin/bash
# Copyright (c) 2021 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

err_exit() {
  echo "$@" 1>&2
  exit 255
}

# external variable check
[ -z "$VERSION_NAME" ] && err_exit 'VERSION_NAME not found'
[ -z "$ASSET_PATH" ]   && err_exit 'ASSET_PATH not found'
[ -z "$CURSE_TOKEN" ]  && err_exit 'CURSE_TOKEN not found'
[ -z "$PRERELEASE" ]   && err_exit 'PRERELEASE not found'
[ -z "$RELEASE_NOTE" ] && err_exit 'RELEASE_NOTE not found'

set -eu

# push
git push
git push origin "$VERSION_NAME"

# platform version infos
# {"id":6756,"gameVersionTypeID":628,"name":"1.12.2","slug":"1-12-2"},
# {"id":7498,"gameVersionTypeID":68441,"name":"Forge","slug":"forge"},
# {"id":4458,"gameVersionTypeID":2,"name":"Java 8","slug":"java-8"},

# project id for fixRTM is 365235
# see https://www.curseforge.com/minecraft/mc-mods/fixrtm
project_id=365235

# curse info
if [ "$PRERELEASE" = "true" ]; then
  release_type="alpha"
else
  release_type="release"
fi

echo "::set-output name=curse_release_type::$release_type"
