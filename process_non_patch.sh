# $1: source file
# $2: dst file
mkdir -p "$(dirname "$2")"
cat "$1" | sed 's/ *$//' | grep -v "this;" >"$2"
