#!/usr/bin/env bash

gsed -e "0,/^## /d" -e '/^## /Q' < "$1"
