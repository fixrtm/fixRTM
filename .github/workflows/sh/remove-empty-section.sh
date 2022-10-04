#!/usr/bin/env bash

INPUT="$1"
temp="$(mktemp)"
cat <"$INPUT" >"$temp"
sed -e "/^###/{
N
/###.*\n$/d
}" <"$temp" >"$INPUT"

rm "$temp"
