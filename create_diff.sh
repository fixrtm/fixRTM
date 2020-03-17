function make_patch() {
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/api/$1"
  (cd "$src_dir" && find . -type f) | while read -r path; do
    ./process_non_patch.sh "$src_dir/$path" "$processed_dir/$path"
  done
  git diff --no-index "$processed_dir" "$dst_dir" > "patches/$1.patch"
}

make_patch ngtlib
make_patch rtm
