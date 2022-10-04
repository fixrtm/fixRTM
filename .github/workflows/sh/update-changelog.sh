#!/usr/bin/env bash

INPUT="$1"
VERSION="$2"
DATE="$3"

LAST_VERSION="$(grep '\[Unreleased\]: ' -A1 < "$INPUT" | tail -1 | sed -E 's/^\[([0-9.]*)]: .*$/\1/')"

temp="$(mktemp)"
cat <"$INPUT" >"$temp"
sed -e "/#* \\[Unreleased]/{
a\\
### Added\\
\\
### Changed\\
\\
### Deprecated\\
\\
### Removed\\
\\
### Fixed\\
\\
### Security\\
\\
## [$VERSION] - $DATE
}
/^\\[Unreleased]/ {
a\\
[Unreleased]: https://github.com/fixrtm/fixRTM/compare/$VERSION...HEAD\\
[$VERSION]: https://github.com/fixrtm/fixRTM/compare/$LAST_VERSION...$VERSION
D
}
" <"$temp" >"$INPUT"

rm "$temp"
