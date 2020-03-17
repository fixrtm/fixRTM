# $1: source file
# $2: dst file
function processNonPatch() {
    mkdir -p "$(dirname "$2")"
    cat "$1" | grep -v "this;" > "$2"
}

function apply_patch() {
  local src_dir dst_dir
  src_dir="mods/$1.deobf.jar.src"
  dst_dir="src/api/$1"
  rm -rf "src/api/$1"
  (cd "$src_dir" && find . -type f) | while read -r path; do
    processNonPatch "$src_dir/$path" "$dst_dir/$path"
  done
  #cp -rf "mods/$1.deobf.jar.src" "src/api/$1"
  patch -p1 < "patches/$1.patch"
}

apply_patch ngtlib
apply_patch rtm
