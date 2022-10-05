#!/usr/bin/env bash

INPUT="$1"
temp="$(mktemp)"
cat <"$INPUT" >"$temp"
sed -e "/^###/{
N
/###.*\n$/d
}" <"$temp" \
  | perl -pe 's/(?<= )`#(\d+)`(?= |$)/[`#$1`](https:\/\/github.com\/fixrtm\/fixRTM\/pull\/$1)/' \
  >"$INPUT"

rm "$temp"
