#!/bin/sh
# Copyright (c) 2022 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

GITHOOKS_DIR="$(dirname $0)"

if ! command -v node > /dev/null 2>&1 ; then
  echo "Please install node.js to run git hooks" >&2
  exit 1
fi

echo "Installing viperproject/check-license-header" >&2

npm install --global \
  @commitlint/config-conventional \
  @commitlint/cli

echo "Installing pre-commit and commit-msg git hook" >&2

copy_hook() {
  ln -s "$GITHOOKS_DIR/$1" "$(git rev-parse --git-dir)/hooks/$1"
}

copy_hook commit-msg
copy_hook pre-commit
