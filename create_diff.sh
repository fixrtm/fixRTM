function make_patch() {
  git diff --no-index "mods/$1.deobf.jar.src" "src/api/$1" > "patches/$1.patch"
}

make_patch ngtlib
make_patch rtm
