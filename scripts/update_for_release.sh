#!/bin/bash
# Copyright (c) 2020 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

set -eu

# 仮ファイルの準備

function rm_tmpfile {
  [[ -f "$1" ]] && rm -f "$1"
}

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

# バージョン情報を作成
if [ "$EVENT_NAME" == push ]; then
  release_kind=commit_release
elif [[ "$VERSION_NAME_IN" == "commit-release" ]]; then
  release_kind=commit_release
elif [[ "$VERSION_NAME_IN" =~ ^[0-9]*\.[0-9]*\.[0-9]*-beta[1-9][0-9]*$ ]]; then
  release_kind=beta_release
elif [[ "$VERSION_NAME_IN" =~ ^[0-9]*\.[0-9]*\.[0-9]*-rc[1-9][0-9]*$ ]]; then
  release_kind=beta_release
elif [[ "$VERSION_NAME_IN" =~ ^[0-9]*\.[0-9]*\.[0-9]*$ ]]; then
  release_kind=stable_release
else
  echo "invalid version name: $version_name"
  exit 255
fi

if [ "$release_kind" == "beta_release" ]; then
    version_name="$VERSION_NAME_IN"
    prerelease=true
    nightly=false
elif [ "$release_kind" == "commit_release" ]; then
    version_name="$(git read-ref HEAD)"
    prerelease=true
    nightly=true
elif [ "$release_kind" == "stable_release" ]; then
    version_name="$VERSION_NAME_IN"
    prerelease=false
    nightly=false
else
  echo "invalid release_kind: $release_kind"
  exit 255
fi

# 各ファイルのバージョン情報更新

if [ "$prerelease" != "true" ]; then
  # update version map

  # masterの行を複製してmasterをバージョン名で置き換える
  cat version-map.md > "$tmp_file"
  awk '{ if (match($0, "master")) { printf("%s%-14s |\n", substr($0, 0, 33), "'"$version_name"'") } ; print }' "$tmp_file" > version-map.md
  rm "$tmp_file"
fi

required_rtm="$(grep master version-map.md | cut -d '|' -f 2 | sed -E 's/^ +| +$//')"
required_ngtlib="$(grep master version-map.md | cut -d '|' -f 3 | sed -E 's/^ +| +$//')"

# gradle.properties を更新
cat gradle.properties > "$tmp_file"
sed 's/^modVersion=.*/modVersion='"$version_name"'/' "$tmp_file" > gradle.properties
rm "$tmp_file"

# コミットとリリース情報を追加

if [ "$prerelease" != "true" ]; then
  changelog_tag_pattern='^[\d.]+$'
  changelog_file_path='CHANGELOG.md'
else
  changelog_tag_pattern='^SNAPSHOT-[0-9-]+$|^[\d.]+(-.*)?$'
  changelog_file_path='CHANGELOG-SNAPSHOTS.md'
fi

if [ "$nightly" != "true" ]; then
  # make release notes commit
  generate-changelog \
    -v "$version_name" \
    --tag-pattern "$changelog_tag_pattern" \
    -o "$changelog_file_path"

  generate-changelog \
    -v "$version_name" \
    -o "$tmp_file"

  # commit to add updated changelog
  git tag -d "$version_name"
  git commit -m "$version_name"
fi

# release noteの抽出

release_note_path=$(mktemp)

if [ "$nightly" != "true" ]; then
  {
    if [ "$prerelease" == "true" ]; then
      echo "**This is SNAPSHOT, not a stable release. make sure this may have many bugs.**"
    fi
    echo "Requirements for this version: "
    echo "RTM $required_rtm"
    echo "NGTLib $required_ngtlib"
    echo ""
    cat "$tmp_file"
  } >>"$release_note_path"
else
  {
    echo "**This is NIGHTLY SNAPSHOT, not a normal SNAPSHOT nor stable release. make sure this may have many bugs.**"
    echo "Requirements for this version: "
    echo "RTM $required_rtm"
    echo "NGTLib $required_ngtlib"
  } >>"$release_note_path"
fi

# 出力設定

asset_path="./build/libs/fixRtm-$version_name.jar"
asset_name="fixRtm-$version_name.jar"

echo "::set-output name=release_note_path::$release_note_path"
echo "::set-output name=version_name::$version_name"
echo "::set-output name=prerelease::$prerelease"
echo "::set-output name=asset_path::$asset_path"
echo "::set-output name=asset_name::$asset_name"
echo "::set-output name=required_rtm::$required_rtm"
echo "::set-output name=required_ngtlib::$required_ngtlib"
echo "::set-output name=nightly::$nightly"
