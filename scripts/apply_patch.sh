DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR/.." || exit

patch_max=${1:-2}

function apply_patch() {
  local processed_dir dst_dir
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/main/$1"
  rm -rf "$dst_dir"
  mv "$processed_dir" "$dst_dir"
}

./scripts/apply_patch_to_processed.sh $patch_max

apply_patch ngtlib
apply_patch rtm
