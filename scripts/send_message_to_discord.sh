#!/bin/bash
# Copyright (c) 2021 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details


err_exit() {
  echo "$@" 1>&2
  exit 255
}

# external variable check
[ -z "$WEBHOOK_URL" ]     && err_exit 'WEBHOOK_URL not found'
[ -z "$TAG_NAME" ]        && err_exit 'TAG_NAME not found'
[ -z "$CURSE_ID" ]        && err_exit 'CURSE_ID not found'
[ -z "$PRERELEASE" ]      && err_exit 'PRERELEASE not found'
[ -z "$REQUIRED_RTM" ]    && err_exit 'REQUIRED_RTM not found'
[ -z "$REQUIRED_NGTLIB" ] && err_exit 'REQUIRED_NGTLIB not found'

function make_temp() {
  local tmp_file_1
  tmp_file_1=$(mktemp)

  # shellcheck disable=SC2064
  trap "rm_tmpfile '$tmp_file_1'" EXIT
  # shellcheck disable=SC2064
  trap "trap - EXIT; rm_tmpfile '$tmp_file_1'; exit -1" INT PIPE TERM

  echo "$tmp_file_1"
}

tmp_file=$(make_temp)

if [ "$PRERELEASE" = true ]; then
    cat <<EOF > "$tmp_file"
SNAPSHOT of fixRTM, $TAG_NAME is released!

**This is SNAPSHOT, not a stable release. make sure this may have many bugs.**
Requirements for this version:
RTM $REQUIRED_RTM
NGTLib $REQUIRED_NGTLIB

https://www.curseforge.com/minecraft/mc-mods/fixrtm/files/$CURSE_ID
https://github.com/fixrtm/fixRTM/releases/tag/$TAG_NAME
EOF
else
    cat <<EOF > "$tmp_file"
fixRTM $TAG_NAME is released!
Requirements for this version:
RTM $REQUIRED_RTM
NGTLib $REQUIRED_NGTLIB

https://www.curseforge.com/minecraft/mc-mods/fixrtm/files/$CURSE_ID
https://github.com/fixrtm/fixRTM/releases/tag/$TAG_NAME
EOF
fi

curl -v -f -X POST \
    --data-urlencode content"@$tmp_file" \
    "$WEBHOOK_URL" \
    || err_exit "POST to curse forge failed"

FILE_ID="$(jq '.id' < response.json)"

echo "::set-output name=file_id::$FILE_ID"
