DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR/.." || exit

function apply_patch() {
  local src_dir processed_dir dst_dir
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/main/$1"

  cp -r "$src_dir" "$processed_dir"

  patch -p1 < "patches/$1.patch"

  rm -rf "$dst_dir"
  mv "$processed_dir" "$dst_dir"
}

apply_patch ngtlib
apply_patch rtm
