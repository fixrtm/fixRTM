#!/bin/bash
# Copyright (c) 2021 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

err_exit() {
  echo "$@" 1>&2
  exit 255
}

# external variable check
[ -z "$ASSET_PATH" ]   && err_exit 'ASSET_PATH not found'
[ -z "$CURSE_TOKEN" ]  && err_exit 'CURSE_TOKEN not found'
[ -z "$RELEASE_NOTE" ] && err_exit 'RELEASE_NOTE not found'
[ -z "$RELEASE_TYPE" ] && err_exit 'RELEASE_TYPE not found'

# platform version infos
# {"id":6756,"gameVersionTypeID":628,"name":"1.12.2","slug":"1-12-2"},
# {"id":7498,"gameVersionTypeID":68441,"name":"Forge","slug":"forge"},
# {"id":4458,"gameVersionTypeID":2,"name":"Java 8","slug":"java-8"},

# project id for fixRTM is 365235
# see https://www.curseforge.com/minecraft/mc-mods/fixrtm
project_id=365235

jq -n -c \
  --rawfile changelog "$RELEASE_NOTE" \
  --arg releaseType "$RELEASE_TYPE" \
  --slurpfile gameVersions <(
    {
      echo "6756"
      echo "7498"
      echo "4458"
    }
  ) \
  '{
      changelog:$changelog,
      changelogType:"markdown",
      releaseType:$releaseType,
      gameVersions:$gameVersions,
  }' \
  > metadata.json

curl -v -f -X POST \
    -F metadata="<metadata.json" \
    -F "file=@\"$ASSET_PATH\"" \
    -H "X-Api-Token: $CURSE_TOKEN" \
    "https://minecraft.curseforge.com/api/projects/$project_id/upload-file" \
    > response.json \
    || err_exit "POST to curse forge failed"

FILE_ID="$(jq '.id' < response.json)"

echo "::set-output name=file_id::$FILE_ID"
