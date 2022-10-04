#!/usr/bin/env bash

sed -e "0,/^## /d" -e '/^## /Q' < "$1"
