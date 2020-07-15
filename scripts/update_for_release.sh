# バージョン情報を作成
if [ "$VERSION_NAME_IN" == "snapshot-generated" ]; then
    version_name="$(date "+SNAPSHOT-%Y-%m-%d-%H-%M-%S")"
    prerelease=true
else
    version_name="$VERSION_NAME_IN"
    prerelease=false
fi

# 仮ファイルの準備

tmp_file=$(mktemp)
function rm_tmpfile {
  [[ -f "$tmp_file" ]] && rm -f "$tmp_file"
}

trap rm_tmpfile EXIT
trap 'trap - EXIT; rm_tmpfile; exit -1' INT PIPE TERM

# 各ファイルのバージョン情報更新

if [ "$prerelease" != "true" ]; then
  # update version map

  # masterの行を複製してmasterをバージョン名で置き換える
  cat version-map.md > "$tmp_file"
  awk '{ if (match($0, "master")) { printf("%s%-14s |\n", substr($0, 0, 33), "'"$version_name"'") } ; print }' "$tmp_file" > version-map.md
  rm "$tmp_file"
fi

# gradle.properties を更新
cat gradle.properties > "$tmp_file"
sed 's/^modVersion=.*/modVersion='"$version_name"'/' "$tmp_file" > gradle.properties
rm "$tmp_file"

# コミット

git commit -am "$version_name"
git push

# 出力設定

asset_path="./build/libs/fixRtm-$version_name.jar"
asset_name="fixRtm-$version_name.jar"

echo "::set-output version_name=$version_name"
echo "::set-output prerelease=$prerelease"
echo "::set-output asset_path=$asset_path"
echo "::set-output asset_name=$asset_name"
