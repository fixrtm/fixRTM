DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR/.." || exit

patch_max=${1:-2}

function apply_patch_0() {
  local src_dir processed_dir
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"
  rm -rf $processed_dir

  (cd "$src_dir" && find . -type f) | while read -r path; do
    ./scripts/process_non_patch.sh "$src_dir/$path" "$processed_dir/$path"
  done
}

function apply_patch() {
  local src_dir processed_dir
  src_dir="mods/$1.deobf.jar.src"
  processed_dir="mods/$1.deobf.jar.src.processed"

  patch -p1 < "patches/$2.$1.patch"
}

apply_patch_0 ngtlib
apply_patch_0 rtm

for i in $(seq 1 $patch_max);do
  apply_patch ngtlib $i
  apply_patch rtm $i
done
