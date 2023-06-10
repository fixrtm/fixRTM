#!/bin/sh
# Copyright (c) 2022 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

set -eu

GITHOOKS_DIR="$(cd "$(dirname $0)" && pwd)"

if ! command -v conventional-commitlint > /dev/null 2>&1 ; then
  echo "Please install conventional-commitlint to run git hooks" >&2
  echo "https://github.com/anatawa12/conventional-commitlint" >&2
  exit 1
fi

echo "Installing pre-commit and commit-msg git hook" >&2

copy_hook() {
  rm "$(git rev-parse --git-dir)/hooks/$1"
  ln -s "$GITHOOKS_DIR/$1" "$(git rev-parse --git-dir)/hooks/$1"
}

copy_hook commit-msg
copy_hook pre-commit
