# Copyright (c) 2021 anatawa12 and other contributors
# This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
# See LICENSE at https://github.com/fixrtm/fixRTM for more details

BEGIN {
  state = "begin"
}
{
  if (state == "match" && match($0, "^####")) state = "end";
  if (state == "match") print;
  if (state == "begin" && index($0, "{MATCH}") != -1) state = "wait-date";
  if (state == "wait-date" && match($0, "^>")) state = "match";
}
