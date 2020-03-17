DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR/.." || exit

patch_max=${1:-2}

function make_patch() {
  processed_dir="mods/$1.deobf.jar.src.processed"
  dst_dir="src/main/$1"
  git diff --no-index "$processed_dir" "$dst_dir" > "patches/$2.$1.patch"
}

./scripts/apply_patch_to_processed.sh $(($patch_max - 1))

make_patch ngtlib $patch_max
make_patch rtm $patch_max
