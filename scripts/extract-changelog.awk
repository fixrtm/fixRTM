BEGIN {
  state = "begin"
}
{
  if (state == "match" && match($0, "^####")) state = "end";
  if (state == "match") print;
  if (state == "begin" && index($0, "{MATCH}") != -1) state = "wait-date";
  if (state == "wait-date" && match($0, "^>")) state = "match";
}
