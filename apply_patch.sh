function apply_patch() {
  rm -rf "src/api/$1"
  mkdir "src/api/"
  cp -rf "mods/$1.deobf.jar.src" "src/api/$1"
  patch -p1 < "patches/$1.patch"
}

apply_patch ngtlib
apply_patch rtm
