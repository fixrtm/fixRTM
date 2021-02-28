#!/bin/bash

err_exit() {
  echo "$@" 1>&2
  exit 255
}

# external variable check
[ -z "$VERSION_NAME" ] || err_exit 'VERSION_NAME not found'
[ -z "$ASSET_PATH" ] || err_exit 'ASSET_PATH not found'

set -eu

# push
git push
git push origin "$VERSION_NAME"
