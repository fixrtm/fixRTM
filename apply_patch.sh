
function apply_patch() {
  local src_dir dst_dir
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/api/$1"
  rm -rf "src/api/$1"
  (cd "$src_dir" && find . -type f) | while read -r path; do
    ./process_non_patch.sh "$src_dir/$path" "$processed_dir/$path"
  done
cp -rf "$processed_dir" "$dst_dir"
  patch -p1 < "patches/$1.patch"
}

apply_patch ngtlib
apply_patch rtm
