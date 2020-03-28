DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR/.." || exit

function make_patch() {
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/main/$1"

  cp -r "$src_dir" "$processed_dir"

  git diff --no-index "$processed_dir" "$dst_dir" > "patches/$1.patch"

  rm -rf "$processed_dir"
}

make_patch ngtlib
make_patch rtm
