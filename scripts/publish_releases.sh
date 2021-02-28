#!/bin/bash

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

if [ "$PRERELEASE" == "true" ]; then
  release_type="release"
else
  release_type="alpha"
fi

metadata_json_path=$(mktemp)

node ./scripts/make_curse_json.js "$metadata_json_path" "$RELEASE_NOTE" 6756,7498,4458 "$release_type"
UPLOAD_URL="https://minecraft.curseforge.com/api/projects/$project_id/upload-file"

curl -sL \
  -X POST \
  -F "metadata=@$metadata_json_path;type=application/json" \
  -F "file=@$ASSET_PATH;type=application/java-archive" \
  -H "X-Api-Token: $CURSE_TOKEN" \
  "$UPLOAD_URL"
